package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.UserMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.UserServiceImpl;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  @DisplayName("회원가입: 새로운 이메일로 가입하면 성공하고, UserDto를 반환해야 한다.")
  void createUserTest() {
    // Given
    UserRegisterRequest request = new UserRegisterRequest("Test@test.com", "Tester",
        "password123!");
    User user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(request.password())
        .build();

    // ReflectionTestUtils로 가짜 ID, createdAt 주입
    UUID uuid = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", uuid);
    ReflectionTestUtils.setField(user, "createdAt", Instant.now());

    UserDto expectedDto = new UserDto(uuid, request.email(), request.nickname(), Instant.now());

    // Mocking
    given(userRepository.existsByEmailAndDeletedAtIsNull(request.email())).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(user)).willReturn(expectedDto);

    // When
    UserDto result = userService.create(request);

    // Then
    assertThat(result.id()).isEqualTo(uuid);
    assertThat(result.email()).isEqualTo(request.email());
    assertThat(result.nickname()).isEqualTo(request.nickname());
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
    String password = "password123!";
    UserLoginRequest request = new UserLoginRequest(email, password);

    User user = User.builder()
        .email(email)
        .nickname("Tester")
        .password(password)
        .build();

    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    ReflectionTestUtils.setField(user, "id", uuid);
    ReflectionTestUtils.setField(user, "createdAt", now);

    UserDto expectedDto = new UserDto(uuid, email, "Tester", now);

    // Mocking
    // 리포지토리 -> 유저 반환
    given(userRepository.findByEmailAndPasswordAndDeletedAtIsNull(email, password)).willReturn(Optional.of(user));
    // 매퍼 -> DTO 반환
    given(userMapper.toDto(user)).willReturn(expectedDto);

    // When
    UserDto result = userService.login(request);

    // Then
    assertThat(result.id()).isEqualTo(uuid);
    assertThat(result.email()).isEqualTo(email);
    assertThat(result.nickname()).isEqualTo("Tester");
  }

  @Test
  @DisplayName("로그인 실패: 잘못된 정보로 로그인을 시도하면 예외가 발생해야 한다.")
  void login_Fail_InvalidUser_Test() {
    // Given - 존재하지 않는 계정 준비
    UserLoginRequest request = new UserLoginRequest("nonUser@test.com", "nonPassword123!");

    // Mocking
    given(userRepository.findByEmailAndPasswordAndDeletedAtIsNull(anyString(), anyString())).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.LOGIN_FAILED);
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
}
