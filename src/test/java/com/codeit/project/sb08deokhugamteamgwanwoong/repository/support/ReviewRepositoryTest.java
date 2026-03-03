package com.codeit.project.sb08deokhugamteamgwanwoong.repository.support;

import com.codeit.project.sb08deokhugamteamgwanwoong.config.JpaConfig;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
//application.yaml 파일을 무시하고 밑에 작성한 코드로 적용함
@TestPropertySource(properties = {
        //data-h2.sql 실행 차단
        "spring.sql.init.mode=never",
        //깨끗한 새 테이블 생성
        "spring.jpa.hibernate.ddl-auto=create"
})
@Import(JpaConfig.class)
public class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("리뷰 저장이 정상적으로 동작해야 한다.")
    void saveReviewTest() {
        //given
        User user = new User("testUser");
        Book book = new Book("testBook");
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
        User user1 = new User("testUser1");
        User user2 = new User("testUser2");
        Book targetBook = new Book("testBook");
        Book otherBook = new Book("testOtherBook");
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
}
