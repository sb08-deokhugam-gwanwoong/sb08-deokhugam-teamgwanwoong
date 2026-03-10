package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.UserMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.UserService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

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
        .password(request.password())
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

    User user = userRepository.findByEmailAndPasswordAndDeletedAtIsNull(request.email(), request.password())
        .orElseThrow(() -> {
          log.warn("[로그인 실패] 이메일 또는 비밀번호가 일치하지 않습니다. email: {}", request.email());
          return new BusinessException(UserErrorCode.LOGIN_FAILED);
        });

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

    log.info("[유저 물리 삭제 완료] userId: {}", userId);
  }

  /**
   * 논리 삭제 후 일정 기간 지난 사용자 물리 삭제 (스케쥴러 전용)
   */
  @Override
  @Transactional
  public void hardDeleteOldUsers() {

    Instant oneDayAgo = Instant.now().minus(Duration.ofDays(1));

    List<User> deleteTargets = userRepository.findAllExpiredUsers(oneDayAgo);

    if (!deleteTargets.isEmpty()) {

      log.info("[물리 삭제 스케쥴러 시작] 삭제 대상 유저 수: {}", deleteTargets.size());

      userRepository.deleteAll(deleteTargets);

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
