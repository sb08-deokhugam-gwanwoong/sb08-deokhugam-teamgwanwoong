package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.UserServiceImpl;
import java.time.Instant;
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

  @InjectMocks
  private UserServiceImpl userService;

  @Test
  @DisplayName("회원가입: 새로운 이메일로 가입하면 성공하고, UserDto를 반환해야 한다.")
  void createUserTest() {
    // Given
    UserRegisterRequest request = new UserRegisterRequest("Test@test.com", "Tester", "password123!");
    User user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(request.password())
        .build();

    // ReflectionTestUtils로 가짜 ID, createdAt 주입
    UUID uuid = UUID.randomUUID();
    ReflectionTestUtils.setField(user, "id", uuid);
    ReflectionTestUtils.setField(user, "createdAt", Instant.now());

    // Mocking
    given(userRepository.existsByEmail(request.email())).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(user);

    // When
    UserDto result = userService.create(request);

    // Then
    assertThat(result.id()).isEqualTo(uuid);
    assertThat(result.email()).isEqualTo(request.email());
    assertThat(result.nickname()).isEqualTo(request.nickname());
  }

}
