package com.codeit.project.sb08deokhugamteamgwanwoong.service.event;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.event.ReviewCreatedEventDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.event.ReviewDeletedEventDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.event.ReviewUpdatedEventDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.event.ReviewSyncEventListener;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class ReveiwSyncEventListenerTest {

    @Mock
    private ReviewSearchRepository reviewSearchRepository;

    @InjectMocks
    private ReviewSyncEventListener reviewSyncEventListener;

    private Review review;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("password01!")
                .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        Book book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("https://test-thumbnail.url/image.jpg")
                .build();
        ReflectionTestUtils.setField(book, "id", UUID.randomUUID());

        review = Review.builder()
                .rating(5)
                .content("재밌어요")
                .user(user)
                .book(book)
                .build();
        ReflectionTestUtils.setField(review, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("리뷰 생성 이벤트 수신 시 ES에 저장한다.")
    void handle_review_created_event() {
        ReviewCreatedEventDto event = new ReviewCreatedEventDto(review);
        reviewSyncEventListener.handleReviewCreated(event);
        then(reviewSearchRepository).should().save(any());
    }

    @Test
    @DisplayName("리뷰 수정 이벤트 수신 시 ES에 업데이트한다.")
    void handle_review_updated_event() {
        ReviewUpdatedEventDto event = new ReviewUpdatedEventDto(review);
        reviewSyncEventListener.handleReviewUpdated(event);
        then(reviewSearchRepository).should().save(any());
    }

    @Test
    @DisplayName("리뷰 삭제 이벤트 수신 시 ES에서 삭제한다.")
    void handle_review_deleted_event() {
        ReviewDeletedEventDto event = new ReviewDeletedEventDto(review.getId());
        reviewSyncEventListener.handleReviewDeleted(event);
        then(reviewSearchRepository).should().deleteById(review.getId().toString());
    }
}
