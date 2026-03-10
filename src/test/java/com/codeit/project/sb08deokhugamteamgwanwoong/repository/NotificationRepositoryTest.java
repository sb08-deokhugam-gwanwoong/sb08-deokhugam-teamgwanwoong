package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseEntity;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class NotificationRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private TestEntityManager em;

  @Nested
  @DisplayName("벌크 연산 테스트: 상태 변경 및 대량 삭제")
  class BulkOperationTest {

    @Test
    @DisplayName("전체 확인: 본인의 미확인 알림만 true로 변경되고, 타인의 알림은 유지되어야 한다.")
    void allConfirmNotification_Test() {
      // Given
      User me = saveUser("test@test.com", "Tester");
      User other = saveUser("other@test.com", "OtherTester");
      Book book = saveBook("9744534657908");
      Review review = saveReview(me, book);

      saveNotification(me, review, false);    // 변경 대상
      saveNotification(me, review, true);     // 유지 대상 (이미 true)
      saveNotification(other, review, false); // 유지 대상 (다른 사람)

      em.flush();
      em.clear();

      // When - 내 ID로 전체 알림 확인 업데이트
      notificationRepository.allConfirmNotification(me.getId());

      // Then
      List<Notification> allNotification = notificationRepository.findAll();

      long confirmedCount = allNotification.stream()
          .filter(n -> n.getUser().getId().equals(me.getId()) && n.isConfirmed())
          .count();

      assertThat(confirmedCount).isEqualTo(2);

      Notification otherNotification = allNotification.stream()
          .filter(n -> n.getUser().getId().equals(other.getId()))
          .findFirst().orElseThrow();

      assertThat(otherNotification.isConfirmed()).isFalse(); // 타인 알림은 그대로 유지
    }

    @Test
    @DisplayName("자동 삭제: 알림 확인이 완료된 상태에서 7일 이상 경과한 알림만 삭제 되어야 한다.")
    void deleteOldConfirmedNotifications_Test() {
      // Given
      User user = saveUser("test@test.com", "Tester");
      Book book = saveBook("9744534657908");
      Review review = saveReview(user, book);

      Instant eightDaysAgo = Instant.now().minus(Duration.ofDays(8));

      saveNotification(user, review, true, eightDaysAgo);                             // 삭제 대상
      Notification nonTarget1 = saveNotification(user, review, false, eightDaysAgo);  // 알림 미확인 대상 (삭제 안됨)
      Notification nonTarget2 = saveNotification(user, review, true, Instant.now()); // 최근 확인 (삭제 안됨)

      em.flush();
      em.clear();

      // When - 7일 전 기준으로 삭제
      notificationRepository.deleteOldConfirmedNotifications(Instant.now().minus(Duration.ofDays(7)));
      
      // Then
      List<Notification> allNotification = notificationRepository.findAll();
      
      assertThat(allNotification.size()).isEqualTo(2);
      assertThat(allNotification).extracting(BaseEntity::getId)
          .containsExactlyInAnyOrder(nonTarget1.getId(), nonTarget2.getId());
    }
  }

  @Nested
  @DisplayName("쿼리 및 페이지네이션 검증")
  class QueryTest {

    @Test
    @DisplayName("카운트 조회: 특정 유저의 전체 알림 개수를 정확하게 반환해야 한다.")
    void countByUserIdTest() {
      // Given
      User user = saveUser("count@test.com", "countTester");
      Book book = saveBook("9744534657908");
      Review review = saveReview(user, book);
      saveNotification(user, review, false);
      saveNotification(user, review, true);

      em.flush();
      em.clear();

      // When - 특정 유저에 대한 전체 알림 개수 조회
      long count = notificationRepository.countByUserId(user.getId());

      // Then
      assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("커서 기반 조회: 정렬 방향(DESC)에 따라 데이터가 정확하게 페이징 되어야 한다.")
    void findAllNotificationCursor_DESC_Test() {
      // Given
      User user = saveUser("count@test.com", "countTester");
      Book book = saveBook("9744534657908");
      Review review = saveReview(user, book);
      Instant now = Instant.now();

      Notification oldNotification = saveNotification(user, review, false, now.minus(Duration.ofHours(3)));
      Notification middleNotification = saveNotification(user, review, false, now.minus(Duration.ofHours(1)));
      Notification newNotification = saveNotification(user, review, false, now);

      em.flush();
      em.clear();

      // When
      Pageable pageable = PageRequest.of(0, 1, Sort.by(Direction.DESC, "createdAt"));

      List<Notification> result = notificationRepository.findAllNotification(user.getId(),
          newNotification.getCreatedAt(), newNotification.getCreatedAt(), pageable);

      // Then - new보다 과거인 middle, old가 조회되어야 한다. (Notification에 기본적으로 +1이 되어 있음.)
      assertThat(result.size()).isEqualTo(2);
      assertThat(result.get(0).getId()).isEqualTo(middleNotification.getId());
      assertThat(result.get(1).getId()).isEqualTo(oldNotification.getId());
    }

    @Test
    @DisplayName("커서 기반 조회: 정렬 방향(ASC)에 따라 데이터가 정확하게 페이징 되어야 한다.")
    void findAllNotificationCursor_ASC_Test() {
      // Given
      User user = saveUser("count@test.com", "countTester");
      Book book = saveBook("9744534657908");
      Review review = saveReview(user, book);
      Instant now = Instant.now();

      saveNotification(user, review, false, now.minus(Duration.ofHours(3)));
      Notification middleNotification = saveNotification(user, review, false, now.minus(Duration.ofHours(1)));
      Notification newNotification = saveNotification(user, review, false, now);

      em.flush();
      em.clear();

      // When
      Pageable pageable = PageRequest.of(0, 1, Sort.by(Direction.ASC, "createdAt"));

      List<Notification> result = notificationRepository.findAllNotification(user.getId(),
          middleNotification.getCreatedAt(), middleNotification.getCreatedAt(), pageable);

      // Then - middle보다 미래인 new가 조회되어야 한다. (Notification에 기본적으로 +1이 되어 있음.)
      assertThat(result.size()).isEqualTo(1);
      assertThat(result.get(0).getId()).isEqualTo(newNotification.getId());
    }

    @Test
    @DisplayName("커서 기반 조회: 커서(cursor, after)가 null이면 전체 조건을 타지 않고 데이터를 조회한다.")
    void findAllNotification_NullCursor_Test() {
      // Given
      User user = saveUser("null@test.com", "nullTester");
      Book book = saveBook("1234567890123");
      Review review = saveReview(user, book);
      saveNotification(user, review, false);
      saveNotification(user, review, false);

      em.flush();
      em.clear();

      // When - 커서에 null 전달
      Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"));

      List<Notification> result = notificationRepository.findAllNotification(user.getId(), null, null, pageable);

      // Then - 조건 없이 해당 유저의 모든 알림(2개)이 조회되어야 한다.
      assertThat(result.size()).isEqualTo(2);
    }
  }

  private User saveUser(String email, String nickname) {
    User user = User.builder()
        .email(email)
        .nickname(nickname)
        .password("password12345!")
        .build();

    return em.persistAndFlush(user);
  }

  private Book saveBook(String isbn) {
    Book book = Book.builder()
        .title("테스트의 정석")
        .author("테스터")
        .isbn(isbn)
        .publisher("테스출판")
        .publishedDate(LocalDate.of(2023, 3, 9))
        .description("테스트 기초서")
        .build();

    return em.persistAndFlush(book);
  }

  private Review saveReview(User user, Book book) {
    Review review = Review.builder()
        .rating(5)
        .content("최고의 책입니다.")
        .user(user)
        .book(book)
        .build();

    return em.persistAndFlush(review);
  }

  private void saveNotification(User user, Review review, boolean isConfirmed) {
    saveNotification(user, review, isConfirmed, Instant.now());
  }

  private Notification saveNotification(User user, Review review, boolean isConfirmed, Instant createdAt) {
    Notification notification = Notification.builder()
        .user(user)
        .review(review)
        .message("[Tester]님이 나의 리뷰를 좋아합니다.")
        .reviewContent(review.getContent())
        .build();

    notification.confirm(isConfirmed);

    // 먼저 영속화 시켜준다. (이때 Auditing에 의해 현재 시간으로 세팅됨)
    em.persist(notification);

    // DB에 반영
    em.flush();;

    // 네이티브 쿼리로 DB의 값을 직접 수정
    em.getEntityManager().createNativeQuery(
        "UPDATE notifications SET created_at = :createdAt WHERE id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", notification.getId())
        .executeUpdate();

    // 자바 객체와 영속성 컨텍스트의 상태도 맞춰준다.
    em.clear();

    // DB에서 수정된 데이터를 다시 읽어와서 리턴 (Java 객체 동기화)
    return em.find(Notification.class, notification.getId());
  }
}
