package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.ReviewLike;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ReviewLikeRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private Book book;
    private Review review;

    @BeforeEach
    public void setUp() {
        user = createUser("test@codeit.com", "testUser");
        book = createBook("testBook", "testAuthor", "9788994492032", "testPublisher", "testDescription", "testThumbnailUrl");
        review = createReview(3, "testReview", user, book);

        entityManager.persist(user);
        entityManager.persist(book);
        entityManager.persist(review);

        flushAndClear();
    }

    @Test
    @DisplayName("리뷰 좋아요는 리뷰 ID와 유저 ID를 통해 조회할 수 있다.")
    void findByReviewIdAndUserIdTest() {
        //given
        ReviewLike reviewLike = createReviewLike(review, user);
        reviewLikeRepository.save(reviewLike);

        flushAndClear();

        //when
        Optional<ReviewLike> foundReviewLike = reviewLikeRepository.findByReviewIdAndUserId(review.getId(), user.getId());

        //then
        assertThat(foundReviewLike).isPresent();

        ReviewLike getReviewLike = foundReviewLike.get();
        assertThat(getReviewLike.getReview().getId()).isEqualTo(review.getId());
        assertThat(getReviewLike.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("해당 리뷰를 좋아요를 누르지 않는 유저의 ID로 좋아요를 조회할 수 없다.")
    void notFoundByReviewIdAndUserIdTest() {
        //given
        User otherUser = createUser("test2@codeit.com", "otherUser");
        ReviewLike reviewLike = createReviewLike(review, user);
        reviewLikeRepository.save(reviewLike);

        flushAndClear();

        //when
        Optional<ReviewLike> foundReviewLike = reviewLikeRepository.findByReviewIdAndUserId(review.getId(), otherUser.getId());

        //then
        assertThat(foundReviewLike).isEmpty();
    }

    @Test
    @DisplayName("특정 리뷰 ID를 가진 모든 리뷰 좋아요 데이터를 물리 삭제할 수 있다.")
    void hardDeleteAllByReviewIdTest() {
        //given
        ReviewLike reviewLike = createReviewLike(review, user);
        reviewLikeRepository.save(reviewLike);

        flushAndClear();

        //when
        reviewLikeRepository.hardDeleteAllByReviewId(review.getId());
        flushAndClear();

        //then
        Optional<ReviewLike> foundReviewLike = reviewLikeRepository.findByReviewIdAndUserId(review.getId(), user.getId());

        assertThat(foundReviewLike).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 ID로 좋아요 삭제를 시도하면 아무 데이터도 삭제되지 않는다.")
    void hardDeleteAllByReviewId_WithoutNonExistReviewIdTest() {
        //given
        ReviewLike reviewLike = createReviewLike(review, user);
        reviewLikeRepository.save(reviewLike);

        flushAndClear();

        UUID fakeReviewId = UUID.randomUUID();

        //when
        reviewLikeRepository.hardDeleteAllByReviewId(fakeReviewId);
        flushAndClear();

        //then
        assertThat(reviewLikeRepository.findByReviewIdAndUserId(review.getId(), user.getId())).isPresent();
    }

    @Test
    @DisplayName("특정 유저가 좋아요를 누른 리뷰 ID의 목록을 조회할 수 있다.")
    void findReviewLikeByReviewIdsTest() {
        Book book2 = createBook("testBook2", "testAuthor2", "9788994492033", "testPublisher2", "testDescription2", "testThumbnailUrl2");
        Book book3 = createBook("testBook3", "testAuthor3", "9788994492034", "testPublisher3", "testDescription3", "testThumbnailUrl3");
        Book book4 = createBook("testBook4", "testAuthor4", "9788994492035", "testPublisher4", "testDescription4", "testThumbnailUrl4");
        entityManager.persist(book2);
        entityManager.persist(book3);
        entityManager.persist(book4);

        Review review2 = createReview(2, "so so...", user, book2);
        Review review3 = createReview(4, "무난합니다.", user, book3);
        Review unlikedReview = createReview(1, "정말 별로입니다...", user, book4);
        reviewRepository.save(review2);
        reviewRepository.save(review3);
        reviewRepository.save(unlikedReview);

        ReviewLike reviewLike = createReviewLike(review, user);
        ReviewLike reviewLike2 = createReviewLike(review2, user);
        ReviewLike reviewLike3 = createReviewLike(review3, user);
        reviewLikeRepository.save(reviewLike);
        reviewLikeRepository.save(reviewLike2);
        reviewLikeRepository.save(reviewLike3);

        flushAndClear();

        List<UUID> reviewIds = List.of(review.getId(), review2.getId(), review3.getId(), unlikedReview.getId());

        //when
        Set<UUID> likedReviewIds = reviewLikeRepository.findLikedReviewIds(user.getId(), reviewIds);

        //then
        assertThat(likedReviewIds).hasSize(3);
        //순서와 상관없이 정확하게 요소들이 있는지 확인
        assertThat(likedReviewIds).containsExactlyInAnyOrder(review.getId(), review2.getId(), review3.getId());

        assertThat(likedReviewIds).doesNotContain(unlikedReview.getId());
    }

    @Test
    @DisplayName("요청한 리뷰 목록 중 유저가 좋아요를 누른 리뷰가 없다면 빈 컬렉션을 반환한다.")
    void findReviewLikeByReviewIds_WhenNolikes_ReturnEmptySetTest() {
        //given
        List<UUID> reviewIds = List.of(review.getId());

        //when
        Set<UUID> likedReviewIds = reviewLikeRepository.findLikedReviewIds(user.getId(), reviewIds);

        //then
        assertThat(likedReviewIds).isEmpty();
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

    private ReviewLike createReviewLike(Review review, User user) {
        return ReviewLike.builder()
                .review(review)
                .user(user)
                .build();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
