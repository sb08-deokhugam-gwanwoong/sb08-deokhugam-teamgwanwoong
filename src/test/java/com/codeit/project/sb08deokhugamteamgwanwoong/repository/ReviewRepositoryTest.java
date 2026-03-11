package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

    private User user;
    private Book book;

    private User secondUser;
    private Book secondBook;
    private Review firstReview, secondReview, thirdReview;

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

    @BeforeEach
    public void setUp() throws InterruptedException {
        user = createUser("test@codeit.com", "testUser");
        book = createBook("testBook", "testAuthor", "9788994492032", "testPublisher", "testDescription", "testThumbnailUrl");

        entityManager.persist(user);
        entityManager.persist(book);

        flushAndClear();
    }

    @Test
    @DisplayName("리뷰 저장이 정상적으로 동작해야 한다.")
    void saveReviewTest() {
        //given
        Review review = createReview(5, "정말 재밌어요!!!", user, book);

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
        User user2 = createUser("test2@codeit.com", "testUser2");
        Book otherBook = createBook("testBook2", "testAuthor2", "9788994492033", "testPublisher2", "testDescription2", "testThumbnailUrl2");
        entityManager.persist(user2);
        entityManager.persist(otherBook);

        Review review1 = createReview(5, "정말 유익한 책입니다.", user, book);
        Review review2 = createReview(4, "도움 되는 책이에요.", user2, book);
        Review review3 = createReview(1, "돈이 아까워요....", user, otherBook);
        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        flushAndClear();

        //when
        List<Review> reviews = reviewRepository.findAllByBookId(book.getId());

        //then
        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0).getRating()).isEqualTo(5);
        assertThat(reviews.get(1).getContent()).isEqualTo("도움 되는 책이에요.");
    }

    @Test
    @DisplayName("책 ID가 존재하지 않을 경우 빈 리뷰 리스트를 반환한다")
    void canNotFoundFindByBookIdReviewsTest() {
        // given
        User user2 = createUser("test2@codeit.com", "testUser2");
        Book otherBook = createBook("testBook2", "testAuthor2", "9788994492033", "testPublisher2", "testDescription2", "testThumbnailUrl2");
        entityManager.persist(user2);
        entityManager.persist(otherBook);

        Review review1 = createReview(5, "정말 유익한 책입니다.", user, book);
        Review review2 = createReview(1, "도움 안되는 책이에요..", user2, book);
        reviewRepository.save(review1);
        reviewRepository.save(review2);

        flushAndClear();

        // when
        List<Review> reviews = reviewRepository.findAllByBookId(otherBook.getId());

        // then
        assertThat(reviews).isEmpty();
    }

    @Test
    @DisplayName("각 도서에 대해 사용자별로 단 하나의 리뷰만 작성할 수 있다.")
    void cannotWriteMultipleReviewsForSameBook() {
        //given
        Review review1 = createReview(3, "평범한 책이에요.", user, book);
        reviewRepository.save(review1);
        entityManager.flush();

        //when
        Review review2 = createReview(1, "한달 후기: 정말 별로네요...", user, book);

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
        Review review = createReview(5, "비관적 락 테스트용 리뷰", user, book);
        Review savedReview = reviewRepository.save(review);

        flushAndClear();

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
        Review review = createReview(5, "비관적 락 테스트용 리뷰", user, book);
        Review savedReview = reviewRepository.save(review);

        flushAndClear();

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
        Review review = createReview(5, "작성자 ID와 책 ID를 통해 조회하는 테스트", user, book);
        reviewRepository.save(review);

        flushAndClear();

        //when
        Optional<Review> foundReview = reviewRepository.findByBookIdAndUserIdIncludeDeleted(book.getId(), user.getId());

        //then
        assertThat(foundReview).isPresent();
        assertThat(foundReview.get().getContent()).isEqualTo("작성자 ID와 책 ID를 통해 조회하는 테스트");
    }

    @Test
    @DisplayName("유저가 해당하는 책에 작성한 리뷰가 없다면 false를 반환한다.")
    void notExistsByBookIdAndUserIdTest() {
        //given
        User otherUser = createUser("test2@codeit.com", "testUser2");
        entityManager.persist(otherUser);

        Review review = createReview(5, "작성자 ID와 책 ID를 통해 조회하는 테스트", user, book);
        reviewRepository.save(review);

        flushAndClear();

        //when
        Optional<Review> foundReview = reviewRepository.findByBookIdAndUserIdIncludeDeleted(book.getId(), otherUser.getId());

        //then
        assertThat(foundReview).isEmpty();
    }

    @Test
    @DisplayName("작성된 리뷰는 새로운 내용으로 수정할 수 있다.")
    void updateReviewTest() {
        //given
        Review review = createReview(5, "초반 내용을 읽고 있는데 알찬 내용이 담겨있습니다.", user, book);
        Review savedReview = reviewRepository.save(review);
        Review foundReview = reviewRepository.findById(savedReview.getId()).orElseThrow();

        //when
        foundReview.update(2, "읽어보니 별로에요...");

        flushAndClear();

        //then
        Review updatedReview = reviewRepository.findById(savedReview.getId()).orElseThrow();
        assertThat(updatedReview.getRating()).isEqualTo(2);
        assertThat(updatedReview.getContent()).isEqualTo("읽어보니 별로에요...");
    }

    @Test
    @DisplayName("작성된 리뷰는 삭제할 수 있다.")
    void deleteReviewTest() {
        //given
        Review review = createReview(3, "평범한 책이네요.", user, book);
        Review savedReview = reviewRepository.save(review);

        //when
        reviewRepository.deleteById(savedReview.getId());
        flushAndClear();

        //then
        boolean exists = reviewRepository.existsById(savedReview.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("논리적 삭제가된 경우에도 리뷰를 조회할 수 있다.")
    void findByIdWithLogicalDelete() {
        //given
        Review review = createReview(3, "평범한 책이네요.", user, book);
        Review savedReview = reviewRepository.save(review);

        //when
        //Repository에 논리적 삭제가 없기 때문에 강제로 네이티브 쿼리 수행
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE reviews SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id")
                .setParameter("id", savedReview.getId())
                .executeUpdate();
        flushAndClear();

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
        Review review = createReview(3, "평범한 책이네요.", user, book);
        Review savedReview = reviewRepository.save(review);

        //when
        //Repository에 논리적 삭제가 없기 때문에 강제로 네이티브 쿼리 수행
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE reviews SET deleted_at = CURRENT_TIMESTAMP WHERE id = :id")
                .setParameter("id", savedReview.getId())
                .executeUpdate();
        flushAndClear();

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
        Review review = createReview(3, "평범한 책이네요.", user, book);
        Review savedReview = reviewRepository.save(review);

        //when
        reviewRepository.hardDeleteById(savedReview.getId());
        flushAndClear();

        //then
        assertThat(reviewRepository.existsById(savedReview.getId())).isFalse();

        Optional<Review> nativeFind = reviewRepository.findByIdIncludeDeleted(savedReview.getId());
        assertThat(nativeFind).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 리뷰를 물리삭제하면, 어떠한 데이터도 삭제되지 않는다.")
    void hardDeleteById_WithNonExistsId_DeleteNothing() {
        //given
        Review review = createReview(3, "평범한 책이네요.", user, book);
        Review savedReview = reviewRepository.save(review);

        flushAndClear();

        //가짜 리뷰 아이디를 생성하여 물리 삭제
        UUID fakeReviewId = UUID.randomUUID();
        reviewRepository.hardDeleteById(fakeReviewId);

        flushAndClear();

        //가짜 리뷰 아이디를 삭제했을 경우 저장된 리뷰가 지워지지 않았는 지 체크
        Optional<Review> stillExistsReview = reviewRepository.findByIdIncludeDeleted(savedReview.getId());

        assertThat(stillExistsReview).isPresent();
        assertThat(stillExistsReview.get().getContent()).isEqualTo("평범한 책이네요.");
    }

    @Test
    @DisplayName("QueryDsl: 조건이 모두 기본(created, DESC)일 때 전체 조회")
    void findAllByCursorWithNoCondition() throws InterruptedException {
        //given
        initQueryDslTestData();

        ReviewSearchCondition condition = new ReviewSearchCondition(
                null, null, null, null, null, "createdAt", Sort.Direction.DESC
        );
        PageRequest pageable = PageRequest.of(0, 20);

        //when
        List<Review> results = reviewRepository.findAllByCursor(condition, pageable);

        //then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getId()).isEqualTo(thirdReview.getId());
    }

    @Test
    @DisplayName("QueryDsl: userId와 bookId가 완전 일치 검색")
    void findAllByUserIdAndBookId() throws InterruptedException {
        //given
        initQueryDslTestData();

        ReviewSearchCondition condition = new ReviewSearchCondition(
                user.getId(), book.getId(), null, null, null, "createdAt", Sort.Direction.DESC
        );
        PageRequest pageable = PageRequest.of(0, 20);

        //when
        List<Review> results = reviewRepository.findAllByCursor(condition, pageable);

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(firstReview.getId());
    }

    @Test
    @DisplayName("QueryDsl: keyword를 통한 검색(닉네임, 책이름, 리뷰내용 부분 일치)")
    void findAllByCursorWithKeyword() throws InterruptedException {
        //given
        initQueryDslTestData();

        ReviewSearchCondition condition = new ReviewSearchCondition(
                null, null, "testUser", null, null, "createdAt", Sort.Direction.DESC
        );
        PageRequest pageable = PageRequest.of(0, 20);

        //when
        List<Review> results = reviewRepository.findAllByCursor(condition, pageable);

        //then
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("QueryDsl: 평점순(rating) 내림차순 DESC + 커서페이징")
    void findAllByCursorWithRatingAndDESC() throws InterruptedException {
        //given
        initQueryDslTestData();

        ReviewSearchCondition conditon = new ReviewSearchCondition(
                null, null, null, "5", firstReview.getCreatedAt(), "rating", Sort.Direction.DESC
        );
        PageRequest pageable = PageRequest.of(0, 20);

        //when
        //5점 이하이면서 첫 리뷰 생성일보다 과거인 경우
        List<Review> results = reviewRepository.findAllByCursor(conditon, pageable);

        //then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(secondReview.getId());
        assertThat(results.get(1).getId()).isEqualTo(thirdReview.getId());
    }

    @Test
    @DisplayName("QueryDsl: 평점순(rating) 오름차순 ASC + 커서 페이징")
    void findAllByCursorWithRatingAndASC() throws InterruptedException {
        //given
        initQueryDslTestData();

        ReviewSearchCondition conditon = new ReviewSearchCondition(
                null, null, null, "2", thirdReview.getCreatedAt(), "rating", Sort.Direction.ASC
        );
        PageRequest pageable = PageRequest.of(0, 20);

        List<Review> results = reviewRepository.findAllByCursor(conditon, pageable);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(secondReview.getId());
        assertThat(results.get(1).getId()).isEqualTo(firstReview.getId());
    }

    @Test
    @DisplayName("QueryDsl: 최신순(createdAt) 내림차순 DESC + 커서 페이징")
    void findAllByCursorWithCreatedAtAndDESC() throws InterruptedException {
        //given
        initQueryDslTestData();

        String cursorTime = secondReview.getCreatedAt().toString();
        ReviewSearchCondition condition = new ReviewSearchCondition(
                null, null, null, cursorTime, null, "createdAt", Sort.Direction.DESC
        );
        PageRequest pageable = PageRequest.of(0, 20);

        //when
        List<Review> results = reviewRepository.findAllByCursor(condition, pageable);

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(firstReview.getId());
    }

    @Test
    @DisplayName("QueryDsl: 최신순(createdAt) 오름차순 ASC + 커서 페이징")
    void findAllByCursorWithCreatedAtAndASC() throws InterruptedException {
        //given
        initQueryDslTestData();

        String cursorTime = firstReview.getCreatedAt().toString();
        ReviewSearchCondition condition = new ReviewSearchCondition(
                null, null, null, cursorTime, null, "createdAt", Sort.Direction.ASC
        );
        PageRequest pageable = PageRequest.of(0, 20);

        //when
        List<Review> results = reviewRepository.findAllByCursor(condition, pageable);

        //then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(secondReview.getId());
    }

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .password("testPassword!")
                .build();
    }

    private Book createBook(String title, String author, String isbn, String publisher, String description, String thumbnailUrl) {
        return Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .publisher(publisher)
                .publishedDate(LocalDate.now())
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    private Review createReview(int rating, String content, User author, Book book) {
        return Review.builder()
                .rating(rating)
                .content(content)
                .user(author)
                .book(book)
                .build();
    }

    private void initQueryDslTestData() throws InterruptedException {
        secondUser = createUser("test2@codeit.com", "testUser2");
        secondBook = createBook("testBook2", "testAuthor2", "9788994492033", "testPublisher2", "testDescription2", "testThumbnailUrl2");

        entityManager.persist(secondUser);
        entityManager.persist(secondBook);

        firstReview = createReview(5, "정말 유익한 책입니다.", user, book);
        entityManager.persist(firstReview);

        //생성 시간이 겹치기 때문에 10ms씩 지나고 생성
        Thread.sleep(10);

        secondReview = createReview(4, "관웅님이 추천해서 읽었는데 좋아요.", secondUser, book);
        entityManager.persist(secondReview);

        Thread.sleep(10);

        thirdReview = createReview(2, "개인적으로 별로였습니다.", user, secondBook);
        entityManager.persist(thirdReview);

        flushAndClear();

        firstReview = entityManager.find(Review.class, firstReview.getId());
        secondReview = entityManager.find(Review.class, secondReview.getId());
        thirdReview = entityManager.find(Review.class, thirdReview.getId());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}