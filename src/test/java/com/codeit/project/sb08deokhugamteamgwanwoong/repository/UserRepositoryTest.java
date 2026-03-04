package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class UserRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager em;

  @Test
  @DisplayName("회원가입 테스트: 유저 정보를 저장하고, Id로 찾을 수 있어야 한다.")
  void saveUserAndFindByEmailTest() {
    // Given
    String email = "test@test.com";
    User user = User.builder()
        .email(email)
        .nickname("테스터 one")
        .password("testPass1234!")
        .build();

    // When
    User savedUser = em.persistAndFlush(user);
    em.clear(); // 메모리 비우기 -> 다음 조회 반드시 DB 거치도록 강제

    // savedUser는 DB 최신의 데이터이다.
    Optional<User> foundUser = userRepository.findById(savedUser.getId());

    // Then
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getEmail()).isEqualTo(email);
    assertThat(foundUser.get().getId()).isNotNull();
  }

  @Test
  @DisplayName("유저 중복 이메일 방지 테스트: 동일한 이메일로 저장 시 예외가 발생해야 한다.")
  void duplicateEmailSaveTest() {
    // Given
    String email = "test@test.com";
    User user1 = User.builder()
        .email(email)
        .nickname("tester1")
        .password("testPass1")
        .build();

    em.persistAndFlush(user1);

    // When & Then (동일한 이메일로 저장 시도 -> 예외 발생)
    User duplicateUser = User.builder()
        .email(email)
        .nickname("tester2")
        .password("testPass2")
        .build();

    assertThrows(ConstraintViolationException.class, () -> {
      userRepository.save(duplicateUser);
      em.flush();
    });
  }

  @Test
  @DisplayName("사용자 조회: 저장된 유저 -> ID 조회하면 데이터가 일치해야 한다.")
  void findUserByIdTest() {
    // Given
    User user = User.builder()
        .email("test@test.com")
        .nickname("테스터 one")
        .password("testPass1234!")
        .build();

    User savedUser = em.persistAndFlush(user);
    em.clear();

    // When
    User findUser = userRepository.findById(savedUser.getId())
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    // Then
    assertThat(findUser.getEmail()).isEqualTo("test@test.com");
    assertThat(findUser.getNickname()).isEqualTo("테스터 one");
  }

  @Test
  @DisplayName("로그인: 가입된 email과 password 입력 시 해당 유저 객체를 반환해야 한다.")
  void loginUserTest() {
    // Given
    String email = "test@test.com";
    String password = "testPass1234";
    String nickname = "테스터 one";

    User user = User.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .build();

    em.persistAndFlush(user);
    em.clear();

    // When
    Optional<User> result = userRepository.findByEmailAndPassword(email, password);

    // Then
    assertThat(result).isPresent(); // 결과가 존재하는가
    assertThat(result.get().getId())
        .isNotNull()                 // ID가 비어 있지 않은지 확인
        .isInstanceOf(UUID.class);   // ID의 타입 -> UUID 확인
    assertThat(result.get().getEmail()).isEqualTo(email);
    assertThat(result.get().getNickname()).isEqualTo(nickname);
    assertThat(result.get().getPassword()).isEqualTo(password);
  }
}
