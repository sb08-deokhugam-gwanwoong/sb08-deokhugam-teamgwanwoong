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

        //then
        assertThat(savedReview.getId()).isNotNull();
        assertThat(savedReview.getUser().getNickname()).isEqualTo("testUser");
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getContent()).isEqualTo("정말 재밌어요!!!");
    }
}
