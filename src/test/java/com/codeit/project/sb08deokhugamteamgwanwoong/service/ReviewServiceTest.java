package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewLikeRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewLikeRepository reviewLikeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ReviewMapper reviewMapper;

    private ReviewCreateRequest createRequest;
    private ReviewUpdateRequest updateRequest;
    private Review review;
    private User user;
    private Book book;

    @BeforeEach
    void setup() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        book = Book.builder()
                .title("testBook")
                .author("testAuthor")
                .isbn("9788994492032")
                .publisher("testPublisher")
                .publishedDate(LocalDate.now())
                .description("testDescription")
                .thumbnailUrl("https://test-thumbnail.url/image.jpg")
                .build();
        ReflectionTestUtils.setField(book, "id", bookId);

        review = Review.builder()
                .rating(5)
                .content("test review")
                .user(user)
                .book(book)
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);

        createRequest = new ReviewCreateRequest(book.getId(), user.getId(), "test review", 5);
        updateRequest = new ReviewUpdateRequest("new test review", 3);
    }

    @Test
    @DisplayName("리뷰 생성 테스트 - 성공")
    void create_review_success() {
        //given
        // 중복 리뷰인지 체크
        given(reviewRepository.existsByBookIdAndUserId(any(), any())).willReturn(false);
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(bookRepository.findById(any())).willReturn(Optional.of(book));
        given(reviewRepository.save(any(Review.class))).willReturn(review);
        given(reviewMapper.toDto(any(), anyBoolean(), any()))
                .willAnswer(invocation -> createReviewDto(invocation.getArgument(0), invocation.getArgument(1)));

        //when
        ReviewDto result = reviewService.createReview(createRequest);

        //then
        assertThat(result.content()).isEqualTo("test review");
        assertThat(result.bookThumbnailUrl()).isEqualTo("https://test-thumbnail.url/image.jpg");
        then(reviewRepository).should().save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 테스트 - 실패(이미 리뷰를 작성한 경우)")
    void create_review_fail_already_create() {
        //given
        given(reviewRepository.existsByBookIdAndUserId(createRequest.bookId(), createRequest.userId())).willReturn(true);

        //when
        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_ALREADY_EXISTS);

        //then
        then(reviewRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 성공")
    void modify_review_success() {
        //given
        UUID reviewId = review.getId();
        UUID requestUserId = user.getId();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(requestUserId)).willReturn(Optional.of(user));
        given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId)).willReturn(Optional.empty());
        given(reviewMapper.toDto(any(), anyBoolean(), any()))
                .willAnswer(invocation -> createReviewDto(invocation.getArgument(0), invocation.getArgument(1)));

        //when
        ReviewDto result = reviewService.updateReview(reviewId, updateRequest, requestUserId);

        //then
        assertThat(result.rating()).isEqualTo(updateRequest.rating());
        assertThat(result.content()).isEqualTo(updateRequest.content());
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 실패(존재하지 않는 리뷰)")
    void modify_review_fail_not_found() {
        //given
        UUID reviewId = review.getId();
        UUID requestUserId = review.getUser().getId();

        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, updateRequest, requestUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 실패(수정 권한이 없는 경우)")
    void modify_review_fail_not_author() {
        //given
        UUID reviewId = review.getId();
        UUID differentUserId = UUID.randomUUID();

        User differentUser = User.builder().nickname("otherUser").build();
        ReflectionTestUtils.setField(differentUser, "id", differentUserId);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(differentUserId)).willReturn(Optional.of(differentUser));

        //when & then
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, updateRequest, differentUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_EDIT_PERMISSION_DENIED);
    }


    // 리뷰 생성 로직만 적용하면 사용하지 않아도 되지만 수정이 필요한 ReviewDto를 위해 공통 메소드 처리하였음.
    private ReviewDto createReviewDto(Review review, boolean isLiked) {
        return new ReviewDto(
                review.getId(),
                review.getBook().getId(),
                review.getBook().getTitle(),
                review.getBook().getThumbnailUrl(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getContent(),
                review.getRating(),
                review.getLikeCount(),
                review.getCommentCount(),
                isLiked,
                Instant.now(),
                Instant.now()
        );
    }
}