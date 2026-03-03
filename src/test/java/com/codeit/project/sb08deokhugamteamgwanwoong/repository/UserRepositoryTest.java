package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.*;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("회원가입 테스트: 유저 정보를 저장하고, 이메일로 찾을 수 있어야 한다.")
  void saveUserAndFindByEmailTest() {
    // Given
    User user = User.builder()
        .email("test@test.com")
        .nickname("테스터 one")
        .password("testPass1234!")
        .build();

    // When
    User savedUser = userRepository.save(user);

    // Then
    assertThat(savedUser.getId()).isNotNull();
    assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
    assertThat(savedUser.getCreatedAt()).isNotNull();
  }
}
