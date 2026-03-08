package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

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

  @Test
  @DisplayName("로그인 실패: 가입되지 않은 이메일로 로그인 시 빈 결과를 반환해야 한다.")
  void loginUser_Fail_EmailNotFound_Test() {
    // Given
    String nonExistentEmail = "non@test.com";
    String password = "nonPassword1234!";

    // When
    Optional<User> result = userRepository.findByEmailAndPassword(nonExistentEmail, password);

    // Then
    assertThat(result).isEmpty();
    assertThat(result.isPresent()).isFalse();
  }

  @Test
  @DisplayName("닉네임 중복 확인: 존재하는 닉네임 조회 시 true를 반환해야 한다.")
  void existsByNicknameTest() {
    // Given
    String nickname = "중복닉네임";
    User user = User.builder()
        .email("nickname@test.com")
        .nickname(nickname)
        .password("password1234!")
        .build();

    em.persistAndFlush(user);
    em.clear();

    // When
    boolean exists = userRepository.existsByNickname(nickname);
    boolean nonExists = userRepository.existsByNickname("없는닉네임");

    assertThat(exists).isTrue();
    assertThat(nonExists).isFalse();
  }

  @Test
  @DisplayName("논리 삭제 유저 조회: 논리 삭제된 유저도 물리 삭제 전 조회 가능해야 한다.")
  void findByIdIncludeDeletedTest() {
    // Given
    User user = User.builder()
        .email("delete@test.com")
        .nickname("deleteUser")
        .password("password1234!")
        .build();

    User savedUser = em.persistAndFlush(user);

    // 논리 삭제 처리
    savedUser.delete();
    em.flush();
    em.clear();

    // When
    Optional<User> foundUser = userRepository.findByIdIncludeDeleted(savedUser.getId());

    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("논리 삭제 만료 유저 조회: 삭제된 지 1일이 지난 유저만 목록에 포함되어야 한다.")
  void findAllExpiredUsersTest() {
    // Given
    Instant now = Instant.now();
    Instant oneDayAgo = now.minus(Duration.ofDays(1));

    // 만료 유저
    User expiredUser = User.builder()
        .email("expired@test.com")
        .nickname("만료유저")
        .password("password1234!")
        .build();

    ReflectionTestUtils.setField(expiredUser, "deletedAt", now.minus(Duration.ofDays(2)));
    userRepository.save(expiredUser);

    // 만료되지 않은 유저 (5시간 전에 삭제됨, 조회되지 않아야 함)
    User recentDeleteUser = User.builder()
        .email("fiveHour@test.com")
        .nickname("최근삭제유저")
        .password("password1234!")
        .build();

    ReflectionTestUtils.setField(recentDeleteUser, "deletedAt", now.minus(Duration.ofHours(5)));
    userRepository.save(recentDeleteUser);

    // 정상 유저 (삭제되지 않음, 조회되지 않아야 함)
    User normalUser = User.builder()
        .email("normal@test.com")
        .nickname("정상유저")
        .password("password1234!")
        .build();

    userRepository.save(normalUser);

    em.flush();
    em.clear();

    // When - 1일 전을 기준시간으로 만료 유저 조회
    List<User> result = userRepository.findAllExpiredUsers(oneDayAgo);

    // Then
    assertThat(result).hasSize(1); // 2일 전 삭제된 유저 1명만 존재해야 함
    assertThat(result.get(0).getNickname()).isEqualTo("만료유저");
    assertThat(result.get(0).getEmail()).isEqualTo("expired@test.com");
    assertThat(result.get(0).getDeletedAt()).isBeforeOrEqualTo(oneDayAgo);
  }
}
