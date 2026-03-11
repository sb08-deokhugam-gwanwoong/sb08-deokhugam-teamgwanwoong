package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.*;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.ReviewLike;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewLikeMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.*;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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
    @Mock
    private ReviewLikeMapper reviewLikeMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CommentRepository commentRepository;

    private ReviewCreateRequest createRequest;
    private ReviewUpdateRequest updateRequest;
    private Review review;
    private User user;
    private User otherUser;
    private Book book;

    @BeforeEach
    void setup() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        user = User.builder()
                .email("test@codeit.com")
                .nickname("testUser")
                .password("testPassword!")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        otherUser = User.builder()
                .email("test2@codeit.com")
                .nickname("testUser2")
                .password("testPassword!")
                .build();
        ReflectionTestUtils.setField(otherUser, "id", otherUserId);

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
    @DisplayName("리뷰 목록 페이징 조회 - 데이터가 없을 때 (빈 리스트)")
    void find_all_review_empty() {
        //given
        //조건이 모두 Null인 경우, 기본값 처리
        ReviewPageRequest request = new ReviewPageRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                20,
                user.getId()
        );
        given(reviewRepository.findAllByCursor(any(), any())).willReturn(new ArrayList<>());

        //when
        CursorPageResponseReviewDto result = reviewService.findAllReview(request, user.getId());

        //then
        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    @DisplayName("리뷰 목록 페이징 조회 - limit을 초과하여 다음 페이지가 있을 경우(rating 정렬)")
    void find_all_review_has_next_rating_order() {
        //given
        Instant now = Instant.now();
        ReviewPageRequest request = new ReviewPageRequest(
                null,
                null,
                null,
                "rating",
                "ASC",
                "5",
                now.toString(),
                1,
                user.getId()
        );
        List<Review> mockReviews = new ArrayList<>(List.of(review, review));
        given(reviewRepository.findAllByCursor(any(ReviewSearchCondition.class), any(Pageable.class))).willReturn(mockReviews);
        given(reviewLikeRepository.findLikedReviewIds(any(), any())).willReturn(Set.of(review.getId()));
        given(reviewMapper.toDto(any(), anyBoolean(), any())).willReturn(createReviewDto(review, true));

        //when
        CursorPageResponseReviewDto result = reviewService.findAllReview(request, user.getId());

        //then
        assertThat(result.hasNext()).isTrue();
        assertThat(result.content()).hasSize(1);
        assertThat(result.nextCursor()).isEqualTo("5");
    }

    @Test
    @DisplayName("리뷰 생성 테스트 - 성공 (처음 작성하는 경우)")
    void create_review_success() {
        //given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(bookRepository.findById(any())).willReturn(Optional.of(book));
        // 중복 리뷰인지 체크
        given(reviewRepository.findByBookIdAndUserIdIncludeDeleted(any(), any())).willReturn(Optional.empty());
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
    @DisplayName("리뷰 생성 테스트 - 성공 (논리 삭제된 리뷰를 복구하는 경우)")
    void create_review_success_restore_deleted() {
        //given
        Review deletedReview = Review.builder()
                .rating(1)
                .content("old review")
                .user(user)
                .book(book)
                .build();
        deletedReview.delete();

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(bookRepository.findById(any())).willReturn(Optional.of(book));
        // 조회 시 삭제된 리뷰가 나옴
        given(reviewRepository.findByBookIdAndUserIdIncludeDeleted(any(), any())).willReturn(Optional.of(deletedReview));

        given(reviewRepository.save(any(Review.class))).willReturn(review);
        given(reviewMapper.toDto(any(), anyBoolean(), any()))
                .willAnswer(invocation -> createReviewDto(invocation.getArgument(0), invocation.getArgument(1)));

        //when
        ReviewDto result = reviewService.createReview(createRequest);

        //then
        assertThat(deletedReview.getDeletedAt()).isNull();
        assertThat(deletedReview.getContent()).isEqualTo(createRequest.content());
        then(reviewRepository).should().save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 테스트 - 실패(도서 정보 없음")
    void create_review_fail_not_foud_book() {
        //given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(bookRepository.findById(any())).willReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", BookErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰 생성 테스트 - 실패(이미 리뷰를 작성한 경우)")
    void create_review_fail_already_create() {
        //given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(bookRepository.findById(any())).willReturn(Optional.of(book));

        Review existingReview = Review.builder()
                .rating(5)
                .content("exist")
                .user(user)
                .book(book)
                .build();
        given(reviewRepository.findByBookIdAndUserIdIncludeDeleted(createRequest.bookId(), createRequest.userId())).willReturn(Optional.of(existingReview));

        //when
        assertThatThrownBy(() -> reviewService.createReview(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_ALREADY_EXISTS);

        //then
        then(reviewRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("리뷰 좋아요 - 이미 누른 경우(좋아요 취소), 알림 발송 x")
    void create_reviewLike_success() {
        //given
        ReviewLike exitingReviewLike = ReviewLike.builder()
                .review(review)
                .user(user)
                .build();
        given(reviewRepository.findByIdWithPessimisticLock(any())).willReturn(Optional.of(review));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(reviewLikeRepository.findByReviewIdAndUserId(any(), any())).willReturn(Optional.of(exitingReviewLike));
        given(reviewLikeMapper.toDto(any(), any(), anyBoolean())).willReturn(new ReviewLikeDto(review.getId(), user.getId(), false));

        //when
        ReviewLikeDto result = reviewService.createReviewLike(review.getId(), user.getId());

        //then
        assertThat(result.liked()).isFalse();
        then(reviewLikeRepository).should().delete(exitingReviewLike);
        then(reviewLikeRepository).should().flush();
        then(reviewRepository).should().decreaseLikeCount(review.getId());
        then(notificationService).should(never()).createNotification(any(), any(), any());
    }

    @Test
    @DisplayName("리뷰 좋아요 테스트 - 처음 누르는 경우: 본인 리뷰는 알림이 오지 않음")
    void create_reviewLike_success_new_same_user() {
        //given
        given(reviewRepository.findByIdWithPessimisticLock(review.getId())).willReturn(Optional.of(review));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(reviewLikeRepository.findByReviewIdAndUserId(review.getId(), user.getId())).willReturn(Optional.empty());
        given(reviewLikeMapper.toDto(any(), any(), anyBoolean())).willReturn(new ReviewLikeDto(review.getId(), user.getId(), true));

        //when
        ReviewLikeDto result = reviewService.createReviewLike(review.getId(), user.getId());

        //then
        assertThat(result.liked()).isTrue();
        then(reviewLikeRepository).should().saveAndFlush(any(ReviewLike.class));
        then(reviewRepository).should().increaseLikeCount(review.getId());
        then(notificationService).should(never()).createNotification(any(), any(), any());
    }

    @Test
    @DisplayName("리뷰 좋아요 - 다른 사람 리뷰에 처음 좋아요를 누른 경우, 알림 발송")
    void create_reviewLike_success_new_other_user() {
        //when
        given(reviewRepository.findByIdWithPessimisticLock(any())).willReturn(Optional.of(review));
        given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));
        given(reviewLikeRepository.findByReviewIdAndUserId(review.getId(), otherUser.getId())).willReturn(Optional.empty());
        given(reviewLikeMapper.toDto(any(), any(), anyBoolean())).willReturn(new ReviewLikeDto(review.getId(), otherUser.getId(), true));

        //when
        ReviewLikeDto result = reviewService.createReviewLike(review.getId(), otherUser.getId());

        //then
        assertThat(result.liked()).isTrue();
        then(reviewLikeRepository).should().saveAndFlush(any(ReviewLike.class));
        then(reviewRepository).should().increaseLikeCount(review.getId());
        then(notificationService).should().createNotification(any(), any(), anyString());
    }

    @Test
    @DisplayName("리뷰 좋아요 테스트 - 실패(리뷰를 찾을 수 없음)")
    void create_reviewLike_fail_review_not_found() {
        //given
        given(reviewRepository.findByIdWithPessimisticLock(any())).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reviewService.createReviewLike(review.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰 상세 조회 테스트 - 성공")
    void find_detail_review_success() {
        //given
        given(reviewRepository.findById(any())).willReturn(Optional.of(review));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(reviewLikeRepository.findByReviewIdAndUserId(any(), any())).willReturn(Optional.of(ReviewLike.builder().build()));
        given(reviewMapper.toDto(any(), anyBoolean(), any())).willReturn(createReviewDto(review, true));

        //when
        ReviewDto result = reviewService.findDetailReview(review.getId(), user.getId());

        //then
        assertThat(result.likedByMe()).isTrue();
        assertThat(result.content()).isEqualTo(review.getContent());
    }

    @Test
    @DisplayName("리뷰 상세 조회 테스트 - 실패(존재하지 않는 리뷰)")
    void find_detail_review_fail_not_found() {
        //given
        given(reviewRepository.findById(any())).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reviewService.findDetailReview(review.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰 논리 삭제 - 성공")
    void soft_delete_review_success() {
        //given
        given(reviewRepository.findByIdWithPessimisticLock(review.getId())).willReturn(Optional.of(review));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        //when
        reviewService.softDeleteReview(review.getId(), user.getId());

        //then
        then(reviewRepository).should().saveAndFlush(review);
        then(commentRepository).should().softDeleteAllByReviewId(eq(review.getId()), any(Instant.class));
    }

    @Test
    @DisplayName("리뷰 논리 삭제 - 실패(존재하지 않는 리뷰)")
    void soft_delete_review_fail_not_found() {
        //given
        given(reviewRepository.findByIdWithPessimisticLock(review.getId())).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reviewService.softDeleteReview(review.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰 논리 삭제 - 실패(삭제 권한이 없는 유저)")
    void soft_delete_review_fail_no_permission() {
        //given
        given(reviewRepository.findByIdWithPessimisticLock(review.getId())).willReturn(Optional.of(review));
        given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));

        //when & then
        assertThatThrownBy(() -> reviewService.softDeleteReview(review.getId(), otherUser.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_DELETE_PERMISSION_DENIED);
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 성공")
    void modify_review_success() {
        //given
        UUID reviewId = review.getId();
        UUID requestUserId = review.getUser().getId();

        given(reviewRepository.findByIdWithPessimisticLock(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(requestUserId)).willReturn(Optional.of(user));
        given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId)).willReturn(Optional.empty());
        given(reviewMapper.toDto(any(), anyBoolean(), any()))
                .willAnswer(invocation -> createReviewDto(invocation.getArgument(0), invocation.getArgument(1)));

        //when
        ReviewDto result = reviewService.updateReview(reviewId, updateRequest, requestUserId);

        //then
        // 실제 리뷰 Review 엔티티가 상태가 잘 변경되었는지
        assertThat(result.rating()).isEqualTo(updateRequest.rating());
        assertThat(result.content()).isEqualTo(review.getContent());

        // 반환된 ReviewDto가 올바른 값을 가져오는 지
        assertThat(result.rating()).isEqualTo(updateRequest.rating());
        assertThat(result.content()).isEqualTo(updateRequest.content());
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 실패(수정 권한이 없는 경우)")
    void modify_review_fail_not_author() {
        //given
        UUID reviewId = review.getId();
        UUID otherUserId = UUID.randomUUID();

        User differentUser = User.builder().nickname("otherUser").build();
        ReflectionTestUtils.setField(differentUser, "id", otherUserId);

        given(reviewRepository.findByIdWithPessimisticLock(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(otherUserId)).willReturn(Optional.of(differentUser));

        //when & then
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, updateRequest, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_EDIT_PERMISSION_DENIED);
    }

    @Test
    @DisplayName("리뷰 수정 테스트 - 실패(존재하지 않는 리뷰)")
    void modify_review_fail_not_found() {
        //given
        UUID reviewId = review.getId();
        UUID requestUserId = review.getUser().getId();

        given(reviewRepository.findByIdWithPessimisticLock(reviewId)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, updateRequest, requestUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("리뷰 물리 삭제 테스트 - 성공")
    void hard_delete_review_success() {
        //given
        given(reviewRepository.findByIdIncludeDeleted(review.getId())).willReturn(Optional.of(review));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(bookRepository.findById(review.getBook().getId())).willReturn(Optional.of(book));

        //when
        reviewService.hardDeleteReview(review.getId(), user.getId());

        //then
        then(commentRepository).should().hardDeleteAllByReviewId(review.getId());
        then(reviewLikeRepository).should().hardDeleteAllByReviewId(review.getId());
        then(reviewRepository).should().hardDeleteById(review.getId());
    }

    @Test
    @DisplayName("리뷰 물리 삭제 테스트 - 실패(삭제 권한이 없는 유저)")
    void hard_delete_review_fail_no_permission() {
        //given
        given(reviewRepository.findByIdIncludeDeleted(review.getId())).willReturn(Optional.of(review));
        given(userRepository.findById(otherUser.getId())).willReturn(Optional.of(otherUser));

        //when & then
        assertThatThrownBy(() -> reviewService.hardDeleteReview(review.getId(), otherUser.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_DELETE_PERMISSION_DENIED);
    }

    @Test
    @DisplayName("리뷰 물리 삭제 테스트 - 실패(존재하지 않는 리뷰: 논리 삭제된 리뷰 포함)")
    void hard_delete_review_fail_not_found() {
        //given
        given(reviewRepository.findByIdIncludeDeleted(review.getId())).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reviewService.hardDeleteReview(review.getId(), user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.REVIEW_NOT_FOUND);
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