package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewLikeRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.UserServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private ReviewLikeRepository reviewLikeRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private Cache cache;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  @DisplayName("회원가입: 새로운 이메일로 가입하면 성공하고, UserDto를 반환해야 한다.")
  void createUserTest() {
    // Given
    UserRegisterRequest request = new UserRegisterRequest("Test@test.com", "Tester", "password123!");

    // 평문을 받으면 암호화 된 비밀번호를 반환
    given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");

    User user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password("encodedPassword")
        .build();

    // ReflectionTestUtils로 가짜 ID, createdAt 주입
    UUID uuid = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", uuid);
    ReflectionTestUtils.setField(user, "createdAt", Instant.now());

    UserDto expectedDto = new UserDto(uuid, request.email(), request.nickname(), Instant.now());

    // Mocking
    given(userRepository.existsByEmailAndDeletedAtIsNull(request.email())).willReturn(false);
    given(userRepository.existsByNicknameAndDeletedAtIsNull(request.nickname())).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(user)).willReturn(expectedDto);

    // When
    UserDto result = userService.create(request);

    // Then
    assertThat(result.id()).isEqualTo(uuid);
    assertThat(result.email()).isEqualTo(request.email());
    assertThat(result.nickname()).isEqualTo(request.nickname());
    verify(passwordEncoder).encode(request.password());
  }

  @Test
  @DisplayName("회원가입: 이미 존재하는 이메일로 가입을 시도하면 예외가 발생해야 한다.")
  void createUser_Fail_DuplicateEmail_Test() {
    // Given
    String email = "test@test.com";
    UserRegisterRequest request = new UserRegisterRequest(email, "Tester", "password123!");

    // Mocking - 해당 이메일은 존재한다는 가정
    given(userRepository.existsByEmailAndDeletedAtIsNull(email)).willReturn(true);

    // When
    BusinessException exception = assertThrows(BusinessException.class, () -> {
      userService.create(request);
    });

    assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("로그인: 가입된 정보로 로그인을 시도하면 성공하고, UserDto를 반환해야 한다.")
  void login_Success_Test() {
    // Given
    String email = "test@test.com";
    String rawPassword = "rawPassword123!";
    String encodedPassword = "encodedPassword123!";
    UserLoginRequest request = new UserLoginRequest(email, rawPassword);

    User user = User.builder()
        .email(email)
        .nickname("Tester")
        .password(encodedPassword) // DB에는 암호화되어 저장된다.
        .build();

    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    ReflectionTestUtils.setField(user, "id", uuid);
    ReflectionTestUtils.setField(user, "createdAt", now);

    UserDto expectedDto = new UserDto(uuid, email, "Tester", now);

    // Mocking
    // 리포지토리 -> 유저 반환
    given(userRepository.findByEmailAndDeletedAtIsNull(email)).willReturn(Optional.of(user));
    // 비밀번호 매칭 성공
    given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);
    // 매퍼 -> DTO 반환
    given(userMapper.toDto(user)).willReturn(expectedDto);

    // When
    UserDto result = userService.login(request);

    // Then
    assertThat(result.id()).isEqualTo(uuid);
    assertThat(result.email()).isEqualTo(email);
    assertThat(result.nickname()).isEqualTo("Tester");
    verify(passwordEncoder).matches(rawPassword, encodedPassword);
  }

  @Test
  @DisplayName("로그인 실패: 잘못된 정보로 로그인을 시도하면 예외가 발생해야 한다.")
  void login_Fail_InvalidUser_Test() {
    // Given - 존재하지 않는 계정 준비
    UserLoginRequest request = new UserLoginRequest("nonUser@test.com", "nonPassword123!");

    // Mocking
    given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.LOGIN_FAILED);
  }

  @Test
  @DisplayName("로그인 실패: 비밀번호가 일치하지 않으면 예외가 발생해야 한다.")
  void login_Fail_WrongPassword_Test() {
    // Given
    String email = "test@test.com";
    String rawPassword = "rawPassword123!";
    String encodedPassword = "encodedPassword123!";
    UserLoginRequest request = new UserLoginRequest(email, rawPassword);
    User user = User.builder()
        .email(email)
        .nickname("Tester")
        .password(encodedPassword)
        .build();

    // Mocking
    given(userRepository.findByEmailAndDeletedAtIsNull(email)).willReturn(Optional.of(user));
    given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

    // When & Then
    assertThatThrownBy(() ->userService.login(request))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.LOGIN_FAILED);

    verify(passwordEncoder).matches(rawPassword, encodedPassword);
  }

  @Test
  @DisplayName("유저 조회: 존재하는 유저 ID로 조회하면 성공하고, UserDto를 반환해야 한다.")
  void getUserByIdTest() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .email("test@test.com")
        .nickname("Tester")
        .password("password1234!")
        .build();
    UserDto userDto = new UserDto(userId, "test@test.com", "Tester", Instant.now());

    // Mocking
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(userDto);

    // When
    UserDto result = userService.getUserById(userId);

    // Then
    assertThat(result.email()).isEqualTo("test@test.com");
    assertThat(result.nickname()).isEqualTo("Tester");
  }

  @Test
  @DisplayName("유저 조회 실패: 존재하지 않는 유저 ID로 조회하면 예외가 발생해야 한다.")
  void getUserById_Fail_Test() {
    // Given
    UUID userId = UUID.randomUUID();

    // Mocking
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getUserById(userId))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("유저 조회 실패: 유저는 존재하지만 이미 탈퇴(Soft Delete) 상태라면 예외가 발생해야 한다.")
  void getUserById_SoftDeleted_Fail_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder().email("test@test.com").nickname("Tester").build();
    ReflectionTestUtils.setField(user, "deletedAt", Instant.now()); // 탈퇴 처리

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // When & Then
    assertThatThrownBy(() -> userService.getUserById(userId))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("유저 수정: 새로운 닉네임으로 수정하면 성공적으로 반영되어야 한다.")
  void updateUserNickname_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .email("test@test.com")
        .nickname("Tester")
        .password("password1234!")
        .build();
    UserUpdateRequest request = new UserUpdateRequest("NewTester");

    // Mocking
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByNicknameAndDeletedAtIsNull("NewTester")).willReturn(false); // 중복 X

    // When
    userService.update(userId, request);

    // Then
    assertThat(user.getNickname()).isEqualTo("NewTester");
  }

  @Test
  @DisplayName("유저 수정: 기존 닉네임과 동일한 경우 중복 검사 없이 완료되어야 한다.")
  void updateUserNicknameSame_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .email("test@test.com")
        .nickname("SameTester")
        .password("password1234!")
        .build();
    UserUpdateRequest request = new UserUpdateRequest("SameTester");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // When
    userService.update(userId, request);

    // Then - 중복 검사 메서드가 호출되지 않았음을 검증
    verify(userRepository, never()).existsByNicknameAndDeletedAtIsNull("SameTester");
  }

  @Test
  @DisplayName("유저 수정 실패: 이미 사용 중인 닉네임으로 수정하면 예외가 발생해야 한다.")
  void updateUserNickname_Fail_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .email("test@test.com")
        .nickname("Tester")
        .password("password1234!")
        .build();
    UserUpdateRequest request = new UserUpdateRequest("DuplicateTester");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByNicknameAndDeletedAtIsNull("DuplicateTester")).willReturn(true); // 중복 O

    // When & Then
    assertThatThrownBy(() -> userService.update(userId, request))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("논리 삭제: 유저 삭제 호출 시 deletedAt 필드가 채워져야 한다.")
  void deleteUser_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .email("test@test.com")
        .nickname("Tester")
        .password("password12345!")
        .build();

    // Mocking
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // When
    userService.delete(userId);

    // Then
    assertThat(user.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("물리 삭제: 논리 삭제된 유저도 찾아와서 완전히 삭제되어야 한다.")
  void deleteUser_Fail_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .email("test@test.com")
        .nickname("Tester")
        .password("password12345!")
        .build();

    // Mocking
    given(userRepository.findByIdIncludeDeleted(userId)).willReturn(Optional.of(user));

    // When
    userService.hardDelete(userId);

    // Then
    verify(userRepository).delete(user);
  }

  @Test
  @DisplayName("비밀번호 재설정: 유저가 존재하면 새로운 비밀번호로 변경된다.")
  void resetPassword_Test() {
    UserResetPasswordRequest request = new UserResetPasswordRequest("test@test.com", "newPass123!");
    User user = User.builder().email("test@test.com").password("password1234!").build();

    given(userRepository.findByEmailAndDeletedAtIsNull(request.email())).willReturn(Optional.of(user));
    given(passwordEncoder.encode(request.newPassword())).willReturn("encodedNew1234!");

    userService.resetPassword(request);

    assertThat(user.getPassword()).isEqualTo("encodedNew1234!");
  }

  @Test
  @DisplayName("비밀번호 재설정 실패: 유저가 존재하지 않으면 예외가 발생한다.")
  void resetPassword_UserNotFound_Test() {
    UserResetPasswordRequest request = new UserResetPasswordRequest("none@test.com", "newPass123!");
    given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.resetPassword(request))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("비밀번호 수정: 현재 비밀번호가 일치하면 새로운 비밀번호로 암호화하여 저장해야 한다.")
  void updatePassword_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .email("test@test.com")
        .nickname("Tester")
        .password("password1234!")
        .build();
    UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password1234!", "newPassword1234!");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(passwordEncoder.matches(request.currentPassword(), user.getPassword())).willReturn(true);
    given(passwordEncoder.encode(request.newPassword())).willReturn("encodedNew1234!");

    // When
    userService.updatePassword(userId, request);

    // Then
    assertThat(user.getPassword()).isEqualTo("encodedNew1234!");
    verify(passwordEncoder).encode(request.newPassword());
  }

  @Test
  @DisplayName("비밀번호 수정 실패: 현재 비밀번호가 일치하지 않으면 예외가 발생해야 한다.")
  void updatePassword_Fail_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .email("test@test.com")
        .nickname("Tester")
        .password("password1234!")
        .build();
    UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password1234!", "newPassword1234!");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(passwordEncoder.matches(request.currentPassword(), user.getPassword())).willReturn(false);

    // When && Then
    assertThatThrownBy(() -> userService.updatePassword(userId, request))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.WRONG_PASSWORD);
  }

  @Test
  @DisplayName("인증번호 발송: 이메일이 존재하면 인증번호를 생성하여 캐시에 저장하고, 메일을 발송해야 한다.")
  void sendVerificationCode_Test() {
    // Given
    String email = "test@test.com";

    given(userRepository.existsByEmailAndDeletedAtIsNull(email)).willReturn(true);
    given(cacheManager.getCache("verificationCodes")).willReturn(cache);

    // When
    userService.sendVerificationCode(email);

    // Then - 캐시에 저장되었는지 확인
    verify(cache).put(eq(email), anyString());
    // 메일 발송 호출 확인
    verify(emailService).sendVerificationCode(eq(email), anyString());
  }

  @Test
  @DisplayName("인증번호 발송 실패: 가입되지 않은 이메일로 요청하면 예외가 발생해야 한다.")
  void sendVerificationCode_Fail_Test() {
    // Given
    String email = "test@test.com";
    given(userRepository.existsByEmailAndDeletedAtIsNull(email)).willReturn(false);

    // When & Then
    assertThatThrownBy(() -> userService.sendVerificationCode(email))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
  }

  @Test
  @DisplayName("인증번호 검증: 캐시의 인증번호와 입력 인증번호가 일치하면 성공 후 캐시가 삭제 되어야 한다.")
  void verifyCode_Test() {
    // Given
    String email = "test@test.com";
    String code = "123456";
    given(cacheManager.getCache("verificationCodes")).willReturn(cache);
    given(cache.get(email, String.class)).willReturn(code);

    // When
    userService.verifyCode(email, code);

    // Then - 사용 후 캐시 삭제 확인
    verify(cache).evict(eq(email));
  }

  @Test
  @DisplayName("인증번호 검증 실패: 입력한 인증번호가 캐시의 인증번호와 다르면 예외가 발생해야 한다.")
  void verifyCode_Fail_Test() {
    // Given
    String email = "test@test.com";
    String correctCode = "123456";
    String inputCode = "999999";

    given(cacheManager.getCache("verificationCodes")).willReturn(cache);
    given(cache.get(email, String.class)).willReturn(correctCode);

    // When & Then
    assertThatThrownBy(() -> userService.verifyCode(email, inputCode))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.VERIFICATION_CODE_MISMATCH);
  }

  @Test
  @DisplayName("인증번호 검증 실패: 캐시에 번호가 없는(만료) 경우 예외가 발생한다.")
  void verifyCode_Expired_Test() {
    String email = "test@test.com";
    given(cacheManager.getCache("verificationCodes")).willReturn(cache);
    // 캐시 만료 상황
    given(cache.get(email, String.class)).willReturn(null);

    assertThatThrownBy(() -> userService.verifyCode(email, "123456"))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.VERIFICATION_CODE_EXPIRED);
  }

  @Test
  @DisplayName("스케줄러: 삭제 대상 유저가 있으면 삭제 및 리뷰 통계 동기화를 수행한다.")
  void hardDeleteOldUsers_WithTargets_Test() {
    // Given
    List<User> targets = List.of(User.builder().build());
    given(userRepository.findAllExpiredUsers(any(Instant.class))).willReturn(targets);

    // When
    userService.hardDeleteOldUsers();

    // Then
    verify(userRepository).deleteAll(targets);
    verify(reviewRepository).syncAllReviewCommentCounts();
  }
}
