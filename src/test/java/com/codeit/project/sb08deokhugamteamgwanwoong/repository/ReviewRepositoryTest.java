package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 이 테스트 클래스에서만 사용할 QueryDSL 설정을 Import
@Import(ReviewRepositoryTest.QuerydslTestConfig.class)
public class ReviewRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private TestEntityManager entityManager;

    // 테스트 전용 QueryDSL 설정(결합도 최소화)
    @TestConfiguration
    static class QuerydslTestConfig {
        @PersistenceContext
        private EntityManager em;

        @Bean
        public JPAQueryFactory queryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @Test
    @DisplayName("리뷰 저장이 정상적으로 동작해야 한다.")
    void saveReviewTest() {
        //given
        User user = new User("test@codeit.com", "testUser", "testPassword!");
        Book book = new Book("testBook", "testAuthor", "9788994492032", "testPublisher", LocalDate.now(), "testDescription", "testThumbnailUrl");
        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(5)
                .content("정말 재밌어요!!!")
                .user(user)
                .book(book)
                .build();

        //when
        Review savedReview = reviewRepository.save(review);

        //then
        assertThat(savedReview.getId()).isNotNull();
        assertThat(savedReview.getUser().getNickname()).isEqualTo("testUser");
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getContent()).isEqualTo("정말 재밌어요!!!");
    }

    @Test
    @DisplayName("책 ID에 해당하는 모든 리뷰를 가져올 수 있다.")
    void findByBookIdReviewsTest() {
        //given
        User user1 = new User("test@codeit.com", "testUser1", "testPassword!");
        User user2 = new User("test2@codeit.com", "testUser2", "testPassword!");
        Book targetBook = new Book("testBook1", "testAuthor1", "9788994492032", "testPublisher", LocalDate.now(), "testDescription1", "testThumbnailUrl");
        Book otherBook = new Book("testBook2", "testAuthor2", "9788994492033", "testPublisher", LocalDate.now(), "testDescription2", "testThumbnailUrl");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(targetBook);
        entityManager.persist(otherBook);

        Review review1 = Review.builder()
                .rating(5)
                .content("정말 유익한 책입니다.")
                .user(user1)
                .book(targetBook)
                .build();

        Review review2 = Review.builder()
                .rating(4)
                .content("도움 되는 책이에요")
                .user(user2)
                .book(targetBook)
                .build();

        Review review3 = Review.builder()
                .rating(1)
                .content("돈이 아까워요....")
                .user(user1)
                .book(otherBook)
                .build();
        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        entityManager.flush();
        entityManager.clear();

        //when
        List<Review> reviews = reviewRepository.findAllByBookId(targetBook.getId());

        //then
        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0).getRating()).isEqualTo(5);
        assertThat(reviews.get(1).getContent()).isEqualTo("도움 되는 책이에요");
    }

    @Test
    @DisplayName("책 ID가 존재하지 않을 경우 빈 리뷰 리스트를 반환한다")
    void canNotFoundFindByBookIdReviewsTest() {
        // given
        User user1 = new User("test@codeit.com", "testUser1", "testPassword!");
        User user2 = new User("test2@codeit.com", "testUser2", "testPassword!");
        Book targetBook = new Book("testBook1", "testAuthor1", "9788994492032", "testPublisher", LocalDate.now(), "testDescription1", "testThumbnailUrl");
        Book otherBook = new Book("testBook2", "testAuthor2", "9788994492033", "testPublisher", LocalDate.now(), "testDescription2", "testThumbnailUrl");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(targetBook);
        entityManager.persist(otherBook);

        Review review1 = Review.builder()
                .rating(5)
                .content("정말 유익한 책입니다.")
                .user(user1)
                .book(targetBook)
                .build();
        Review review2 = Review.builder()
                .rating(1)
                .content("도움 안되는 책이에요..")
                .user(user2)
                .book(targetBook)
                .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Review> reviews = reviewRepository.findAllByBookId(otherBook.getId());

        // then
        assertThat(reviews).isEmpty();
    }

    @Test
    @DisplayName("각 도서에 대해 사용자별로 단 하나의 리뷰만 작성할 수 있다.")
    void cannotWriteMultipleReviewsForSameBook() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();
        entityManager.persist(user);
        entityManager.persist(book);

        Review review1 = Review.builder()
                .rating(3)
                .content("평범한 책이에요.")
                .user(user)
                .book(book)
                .build();
        reviewRepository.save(review1);
        entityManager.flush();

        //when
        Review review2 = Review.builder()
                .rating(1)
                .content("한달 후기: 정말 별로네요...")
                .user(user)
                .book(book)
                .build();
        //then
        assertThatThrownBy(() -> {
            reviewRepository.save(review2);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("비관적 락이 적용된 상태로 리뷰를 정상적으로 조회한다.")
    void findByIdWithPessimisticLockTest() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();

        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(5)
                .content("비관적 락 테스트용 리뷰")
                .user(user)
                .book(book)
                .build();
        Review savedReview = reviewRepository.save(review);

        entityManager.flush();
        entityManager.clear();

        Optional<Review> foundReview = reviewRepository.findByIdWithPessimisticLock(savedReview.getId());

        //then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getId()).isEqualTo(savedReview.getId());
        assertThat(foundReview.get().getRating()).isEqualTo(5);
        assertThat(foundReview.get().getContent()).isEqualTo("비관적 락 테스트용 리뷰");

        LockModeType lockmode = entityManager.getEntityManager().getLockMode(foundReview.get());
        assertThat(lockmode).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("일반 findById로 조회하면 비관적 락이 적용되지 않는다.")
    void findByIdWithoutPessimisticLockTest() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();

        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(5)
                .content("비관적 락 테스트용 리뷰")
                .user(user)
                .book(book)
                .build();
        Review savedReview = reviewRepository.save(review);

        entityManager.flush();
        entityManager.clear();

        //when
        Optional<Review> foundReview = reviewRepository.findById(savedReview.getId());

        //then
        assertThat(foundReview).isPresent();

        LockModeType lockmode = entityManager.getEntityManager().getLockMode(foundReview.get());
        assertThat(lockmode).isNotEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(lockmode).isEqualTo(LockModeType.NONE);
    }

    @Test
    @DisplayName("작성된 리뷰는 작성자 ID, 책 ID를 통해 존재 여부를 확인할 수 있다.")
    void existsByBookIdAndUserIdTest() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();

        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(5)
                .content("작성자 ID와 책 ID를 통해 조회하는 테스트")
                .user(user)
                .book(book)
                .build();
        reviewRepository.save(review);

        entityManager.flush();
        entityManager.clear();

        //when
        boolean isPresent = reviewRepository.existsByBookIdAndUserId(book.getId(), user.getId());

        //then
        assertThat(isPresent).isTrue();
    }

    @Test
    @DisplayName("유저가 해당하는 책에 작성한 리뷰가 없다면 false를 반환한다.")
    void notExistsByBookIdAndUserIdTest() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        User otherUser = User.builder()
                .email("test2@codeit.com")
                .nickname("testUser2")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook1")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();

        entityManager.persist(user);
        entityManager.persist(otherUser);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(5)
                .content("작성자 ID와 책 ID를 통해 조회하는 테스트")
                .user(user)
                .book(book)
                .build();
        reviewRepository.save(review);

        entityManager.flush();
        entityManager.clear();

        //when
        boolean isPresent = reviewRepository.existsByBookIdAndUserId(book.getId(), otherUser.getId());

        //then
        assertThat(isPresent).isFalse();
    }

    @Test
    @DisplayName("작성된 리뷰는 새로운 내용으로 수정할 수 있다.")
    void updateReviewTest() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();
        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(5)
                .content("초반 내용을 읽고 있는데 알찬 내용이 담겨있습니다.")
                .user(user)
                .book(book)
                .build();

        Review savedReview = reviewRepository.save(review);
        Review foundReview = reviewRepository.findById(savedReview.getId()).orElseThrow();
        //when
        foundReview.update(2, "읽어보니 별로에요...");

        entityManager.flush();
        entityManager.clear();

        //then
        Review updatedReview = reviewRepository.findById(savedReview.getId()).orElseThrow();
        assertThat(updatedReview.getRating()).isEqualTo(2);
        assertThat(updatedReview.getContent()).isEqualTo("읽어보니 별로에요...");
    }

    @Test
    @DisplayName("작성된 리뷰는 삭제할 수 있다.")
    void deleteReviewTest() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();
        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(3)
                .content("평범한 책이네요.")
                .user(user)
                .book(book)
                .build();
        Review savedReview = reviewRepository.save(review);

        //when
        reviewRepository.deleteById(savedReview.getId());
        entityManager.flush();
        entityManager.clear();

        //then
        boolean exists = reviewRepository.existsById(savedReview.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("논리적 삭제가된 경우에도 리뷰를 조회할 수 있다.")
    void findByIdWithLogicalDelete() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();
        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(3)
                .content("평범한 책이네요.")
                .user(user)
                .book(book)
                .build();
        Review savedReview = reviewRepository.save(review);

        //when
        //Repository에 논리적 삭제가 없기 때문에 강제로 네이티브 쿼리 수행
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE reviews SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id")
                .setParameter("id", savedReview.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        //then
        //findById를 통해 찾을 수 없음
        Optional<Review> standardFind = reviewRepository.findById(savedReview.getId());
        assertThat(standardFind).isEmpty();

        //existsById를 통해 찾을 수 없음
        boolean exists = reviewRepository.existsById(savedReview.getId());
        assertThat(exists).isFalse();

        // 네이티브 쿼리를 통해 조회 가능
        Optional<Review> nativeFind = reviewRepository.findByIdIncludeDeleted(savedReview.getId());
        assertThat(nativeFind).isPresent();
        assertThat(nativeFind.get().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("논리적 삭제가 된 리뷰는 기존 JPA 메소드로 조회할 수 없다.")
    void cannotFindLogicallyDeletedReviewWithStandardJpaMethodsTest() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();
        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(3)
                .content("평범한 책이네요.")
                .user(user)
                .book(book)
                .build();
        Review savedReview = reviewRepository.save(review);

        //when
        //Repository에 논리적 삭제가 없기 때문에 강제로 네이티브 쿼리 수행
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE reviews SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id")
                .setParameter("id", savedReview.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        //then
        //findById를 통해 찾을 수 없음
        Optional<Review> standardFind = reviewRepository.findById(savedReview.getId());
        assertThat(standardFind).isEmpty();

        //existsById를 통해 찾을 수 없음
        boolean exists = reviewRepository.existsById(savedReview.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("작성된 리뷰는 물리 삭제할 수 있다.")
    void hardDeleteById() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();
        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(3)
                .content("평범한 책이네요.")
                .user(user)
                .book(book)
                .build();
        Review savedReview = reviewRepository.save(review);

        //when
        reviewRepository.hardDeleteById(savedReview.getId());
        entityManager.flush();
        entityManager.clear();

        //then
        assertThat(reviewRepository.existsById(savedReview.getId())).isFalse();

        Optional<Review> nativeFind = reviewRepository.findByIdIncludeDeleted(savedReview.getId());
        assertThat(nativeFind).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 리뷰를 물리삭제하면, 어떠한 데이터도 삭제되지 않는다.")
    void hardDeleteById_WithNonExistsId_DeleteNothing() {
        //given
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("testThumbnailUrl")
                .build();
        entityManager.persist(user);
        entityManager.persist(book);

        Review review = Review.builder()
                .rating(3)
                .content("평범한 책이네요.")
                .user(user)
                .book(book)
                .build();
        Review savedReview = reviewRepository.save(review);

        entityManager.flush();
        entityManager.clear();

        //가짜 리뷰 아이디를 생성하여 물리 삭제
        UUID fakeReviewId = UUID.randomUUID();
        reviewRepository.hardDeleteById(fakeReviewId);

        entityManager.flush();
        entityManager.clear();

        //가짜 리뷰 아이디를 삭제했을 경우 저장된 리뷰가 지워지지 않았는 지 체크
        Optional<Review> stillExistsReview = reviewRepository.findByIdIncludeDeleted(savedReview.getId());

        assertThat(stillExistsReview).isPresent();
        assertThat(stillExistsReview.get().getContent()).isEqualTo("평범한 책이네요.");
    }
}