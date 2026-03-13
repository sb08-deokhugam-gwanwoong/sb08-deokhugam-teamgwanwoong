package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserPasswordUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserResetPasswordRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.UserMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.EmailService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final CacheManager cacheManager;
  private final EmailService emailService;

  /**
   * 회원가입
   * @param request 회원가입 요청 request
   * @return UserDto
   */
  @Override
  @Transactional
  public UserDto create(UserRegisterRequest request) {

    log.info("[회원가입 시작] email: {}, nickname: {}", request.email(), request.nickname());

    validateDuplicateEmail(request.email());
    validateDuplicateNickname(request.nickname());

    User user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        // 비밀번호 암호화
        .password(passwordEncoder.encode(request.password()))
        .build();

    User savedUser = userRepository.save(user);

    log.info("[회원가입 완료] 새로운 유저 생성 성공. id: {}", savedUser.getId());

    return userMapper.toDto(savedUser);
  }

  /**
   * 유저 로그인
   * @param request 로그인 요청 request
   * @return UserDto
   */
  @Override
  public UserDto login(UserLoginRequest request) {

    log.info("[로그인 시작] email: {}", request.email());

    User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
        .orElseThrow(() -> {
          log.warn("[로그인 실패] 이메일이 일치하지 않습니다. email: {}", request.email());
          return new BusinessException(UserErrorCode.LOGIN_FAILED);
        });

    // matches()로 비밀번호 일치 여부 확인
    if(!passwordEncoder.matches(request.password(), user.getPassword())) {
      log.warn("[로그인 실패] 비밀번호가 일치하지 않습니다. email: {}", request.password());
      throw new BusinessException(UserErrorCode.LOGIN_FAILED);
    }

    log.info("[로그인 성공] userId: {}, email: {}", user.getId(), user.getEmail());

    return userMapper.toDto(user);
  }

  /**
   * 유저 조회
   * @param userId 유저 Id
   * @return UserDto
   */
  @Override
  public UserDto getUserById(UUID userId) {

    log.info("[유저 조회 시작] userId: {}", userId);

    User user = findUserById(userId, null);

    log.info("[유저 조회 성공] userId: {}, email: {}", user.getId(), user.getEmail());

    return userMapper.toDto(user);
  }

  /**
   * 유저 닉네임 수정
   * @param userId  유저 Id
   * @param request 유저 수정 요청 request
   * @return UserDto
   */
  @Override
  @Transactional
  public UserDto update(UUID userId, UserUpdateRequest request) {

    log.info("[유저 닉네임 수정 시작] userId: {}", userId);

    User user = findUserById(userId, null);

    if (!user.getNickname().equals(request.nickname())) {
      validateDuplicateNickname(request.nickname());
      user.updateNickname(request.nickname());
    } else {
      log.info("[유저 닉네임 수정 건너뜀] 기존 닉네임과 동일합니다. userId: {}", userId);
    }

    log.info("[유저 닉네임 수정 완료] userId: {}, nickname: {}", userId, user.getNickname());

    return userMapper.toDto(user);
  }

  /**
   * 유저 비밀번호 수정
   * @param userId  유저 Id
   * @param request 유저 비밀번호 수정 요청 request
   */
  @Override
  @Transactional
  public void updatePassword(UUID userId, UserPasswordUpdateRequest request) {

    log.info("[유저 비밀번호 수정 시작] userId: {}", userId);

    User user = findUserById(userId, null);

    if(!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new BusinessException(UserErrorCode.WRONG_PASSWORD);
    }

    user.updatePassword(passwordEncoder.encode(request.newPassword()));

    log.info("[유저 비밀번호 수정 완료] userId: {}, nickname: {}", userId, user.getNickname());
  }

  /**
   * 인증번호 생성 및 저장
   * @param email 유저 Email
   */
  @Override
  public void sendVerificationCode(String email) {

    log.info("[인증번호 요청 시작] email: {}", email);

    if (!userRepository.existsByEmailAndDeletedAtIsNull(email)) {
      log.warn("[인증번호 요청 실패] 존재하지 않는 이메일입니다. email: {}", email);
      throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }

    // 랜덤한 숫자 생성 (6자리 고정)
    String verificationCode = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));

    Cache cache = cacheManager.getCache("verificationCodes");

    // Caffeine 캐시에 인증번호 임시 저장
    if (cache != null) {
      cache.put(email, verificationCode);
    }

    // 실제 이메일 요청
    emailService.sendVerificationCode(email, verificationCode);

    log.info("[인증번호 저장 완료] email: {}, code: {}", email, verificationCode);
  }

  /**
   * 인증번호 검증
   * @param email       유저 Email
   * @param inputCode   사용자가 입력한 번호
   */
  @Override
  public void verifyCode(String email, String inputCode) {
    log.info("[인증번호 검증 시작] email: {}", email);

    // Caffeine 캐시에서 인증번호 조회
    Cache cache = cacheManager.getCache("verificationCodes");
    String savedCode = (cache != null) ? cache.get(email, String.class) : null;

    if (savedCode == null) {
      log.warn("[인증번호 검증 실패] 인증번호가 만료되었거나 요청 이력이 없습니다. email: {}", email);
      throw new BusinessException(UserErrorCode.VERIFICATION_CODE_EXPIRED);
    }

    if (!savedCode.equals(inputCode)) {
      log.warn("[인증번호 검증 실패] 인증번호가 일치하지 않습니다. email: {}", email);
      throw new BusinessException(UserErrorCode.VERIFICATION_CODE_MISMATCH);
    }

    log.info("[인증번호 검증 성공] email: {}", email);

    // 1회용 인증 -> 캐시 초기화
    cache.evict(email);
  }

  /**
   * 비밀번호 찾기: 최종 비밀번호 재설정
   * @param request   유저 비밀번호 리셋 요청 request
   */
  @Override
  public void resetPassword(UserResetPasswordRequest request) {

    String email = request.email();
    String newPassword = request.newPassword();

    log.info("[비밀번호 재설정 시작] email: {}", email);

    User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
        .orElseThrow(() -> {
          log.warn("[비밀번호 재설정 실패] 존재하지 않는 유저입니다. email: {}", email);
          return new BusinessException(UserErrorCode.USER_NOT_FOUND);
        });

    String encodedPassword = passwordEncoder.encode(newPassword);

    user.updatePassword(encodedPassword);

    log.info("[비밀번호 재설정 완료] email: {}", email);
  }

  /**
   * 유저 논리 삭제
   * @param userId 유저 Id
   */
  @Override
  @Transactional
  public void delete(UUID userId) {

    log.info("[유저 논리 삭제 시작] userId: {}", userId);

    User user = findUserById(userId, null);

    user.delete();

    log.info("[유저 논리 삭제 완료] userId: {}, deletedAt: {}", userId, user.getDeletedAt());
  }

  /**
   * 유저 물리 삭제
   * @param userId 유저 Id
   */
  @Override
  @Transactional
  public void hardDelete(UUID userId) {

    log.info("[유저 물리 삭제 시작] userId: {}", userId);

    User user = findUserById(userId, "hard");

    userRepository.delete(user);

    reviewRepository.syncAllReviewCommentCounts();

    log.info("[유저 물리 삭제 완료] userId: {}", userId);
  }

  /**
   * 논리 삭제 후 일정 기간 지난 사용자 물리 삭제 (스케쥴러 전용)
   */
  @Override
  @Transactional
  public void hardDeleteOldUsers() {

    Instant oneDayAgo = Instant.now().minus(Duration.ofMinutes(1));

    List<User> deleteTargets = userRepository.findAllExpiredUsers(oneDayAgo);

    if (!deleteTargets.isEmpty()) {

      log.info("[물리 삭제 스케쥴러 시작] 삭제 대상 유저 수: {}", deleteTargets.size());

      userRepository.deleteAll(deleteTargets);

      reviewRepository.syncAllReviewCommentCounts();

      log.info("[물리 삭제 스케쥴러 완료] 논리 삭제 후 1일이 지난 유저 삭제 완료");
    }
  }

  /**
   * 닉네임 중복 검증 (공통 메서드)
   * @param nickname 닉네임
   */
  private void validateDuplicateNickname(String nickname) {
    if (userRepository.existsByNicknameAndDeletedAtIsNull(nickname)) {
      log.warn("[중복 검증 실패] 이미 존재하는 닉네임입니다. nickname: {}", nickname);
      throw new BusinessException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
    }
  }

  /**
   * 이메일 중복 검증 (공통 메서드)
   * @param email 이메일
   */
  private void validateDuplicateEmail(String email) {
    if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
      log.warn("[중복 검증 실패] 이미 존재하는 이메일입니다. email: {}", email);
      throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }
  }

  /**
   * 유저 조회 (공통 메서드)
   * @param userId 유저 Id
   * @return User
   */
  private User findUserById(UUID userId, String type) {

    if ("hard".equals(type)) {
      return userRepository.findByIdIncludeDeleted(userId)
          .filter(u -> u.getDeletedAt() == null) // 탈퇴하지 않은 유저인지 검증
          .orElseThrow(() -> {
            log.warn("[유저 조회 실패] 물리 삭제할 유저가 존재하지 않습니다. userId: {}", userId);
            return new BusinessException(UserErrorCode.USER_NOT_FOUND);
          });
    } else {
      return userRepository.findById(userId)
          .filter(u -> u.getDeletedAt() == null) // 탈퇴하지 않은 유저인지 검증
          .orElseThrow(() -> {
            log.warn("[유저 조회 실패] 해당 유저가 존재하지 않습니다. userId: {}", userId);
            return new BusinessException(UserErrorCode.USER_NOT_FOUND);
          });
    }
  }
}
