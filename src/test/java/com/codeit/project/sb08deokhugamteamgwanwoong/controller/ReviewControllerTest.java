package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.support.ControllerTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.*;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReviewControllerTest extends ControllerTestSupport {

    private UUID reviewId;
    private UUID notFoundReviewId;
    private UUID bookId;
    private UUID userId;
    private UUID otherUserId;
    private UUID requestUserId;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        //존재 하지 않는 리뷰 ID
        notFoundReviewId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        //요청자 ID
        requestUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("GET /api/reviews - 리뷰 목록 조회 성공")
    void getAllReviews_success() throws Exception {
        //given
        ReviewPageRequest pageRequest = createReviewPageRequest(
                null,
                "createdAt",
                "DESC",
                null,
                null,
                20
        );

        ReviewDto reviewDto = createReviewDto(reviewId, bookId, userId);
        CursorPageResponseReviewDto responseDto = new CursorPageResponseReviewDto(List.of(reviewDto), null, null, 20, null, false);

        //BDD 모킹
        given(reviewService.findAllReview(any(ReviewPageRequest.class), eq(requestUserId))).willReturn(responseDto);

        //when & then
        mockMvc.perform(get("/api/reviews")
                        .header("Deokhugam-Request-User-ID", requestUserId.toString())
                        .params(toMultiValueMap(pageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(reviewDto.id().toString()))
                .andExpect(jsonPath("$.content[0].content").value(reviewDto.content()))
                .andExpect(jsonPath("$.hasNext").value(false));

        //BDD 검증
        then(reviewService).should().findAllReview(any(ReviewPageRequest.class), eq(requestUserId));
    }

    @Test
    @DisplayName("GET /api/reviews - 리뷰 목록 조회 실패(헤더 누락)")
    void getAllReviews_fail_missing_header() throws Exception {
        //given
        ReviewPageRequest pageRequest = createReviewPageRequest(null, "createdAt", "DESC", null, null, 20);

        //when & then
        mockMvc.perform(get("/api/reviews")
                        .params(toMultiValueMap(pageRequest)))
                .andExpect(status().isBadRequest());

        then(reviewService).should(never()).findAllReview(any(), any());
    }

    @Test
    @DisplayName("POST /api/reviews - 리뷰 생성 성공")
    void createReview_success() throws Exception {
        //given
        ReviewCreateRequest request = createReviewCreateRequest(5);
        ReviewDto reviewDto = createReviewDto(reviewId, bookId, userId);

        //BDD 모킹
        given(reviewService.createReview(any(ReviewCreateRequest.class))).willReturn(reviewDto);

        //when & then
        mockMvc.perform(post("/api/reviews")
                        .header("Deokhugam-Request-User-ID", requestUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reviewDto.id().toString()))
                .andExpect(jsonPath("$.rating").value(reviewDto.rating()))
                .andExpect(jsonPath("$.content").value(reviewDto.content()));

        //BDD 검증
        then(reviewService).should().createReview(argThat(req ->
                req.rating().equals(request.rating()) && req.content().equals(request.content())
        ));
    }

    @Test
    @DisplayName("POST /api/reviews - 리뷰 생성 실패(유효성 검사: 범위에 벗어난 평점)")
    void createReview_fail_validation() throws Exception {
        //given
        ReviewCreateRequest request = createReviewCreateRequest(6);

        //when & then
        mockMvc.perform(post("/api/reviews")
                        .header("Deokhugam-Request-User-ID", requestUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(reviewService).should(never()).createReview(any());
    }

    @Test
    @DisplayName("POST /api/reviews - 리뷰 생성 실패(존재하지 않는 도서)")
    void createReview_fail_missing_book() throws Exception {
        //given
        ReviewCreateRequest request = createReviewCreateRequest(5);

        //BDD 모킹
        given(reviewService.createReview(any(ReviewCreateRequest.class)))
                .willThrow(new BusinessException(BookErrorCode.BOOK_NOT_FOUND));

        //when & then
        mockMvc.perform(post("/api/reviews")
                        .header("Deokhugam-Request-User-ID", requestUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        // 서비스 호출이 1번 실행됐는지 검증
        then(reviewService).should(times(1)).createReview(any(ReviewCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/reviews - 리뷰 생성 실패(이미 작성된 리뷰 존재)")
    void createReview_fail_already_exists() throws Exception {
        ReviewCreateRequest request = createReviewCreateRequest(5);
        createReviewDto(reviewId, bookId, userId);

        //BDD 모킹
        given(reviewService.createReview(any(ReviewCreateRequest.class)))
                .willThrow(new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS));

        //when & then
        mockMvc.perform(post("/api/reviews")
                        .header("Deokhugam-Request-User-ID", requestUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        // 서비스 호출이 2번 실행됐는지 검증
        then(reviewService).should(times(1)).createReview(any(ReviewCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/reviews/{reviewId}/like - 리뷰 좋아요 성공")
    void createReviewLike_success() throws Exception {
        //given
        ReviewLikeDto reviewLikeDto = new ReviewLikeDto(reviewId, requestUserId, true);

        //BDD 모킹
        given(reviewService.createReviewLike(reviewId, requestUserId)).willReturn(reviewLikeDto);

        //when & then
        mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.userId").value(requestUserId.toString()));

        //BDD 검증
        then(reviewService).should().createReviewLike(reviewId, requestUserId);
    }

    @Test
    @DisplayName("POST /api/reviews/{reviewId}/like - 리뷰 좋아요 실패(헤더 누락)")
    void createReviewLike_fail_missing_header() throws Exception {
        //when & then
        mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId))
                .andExpect(status().isBadRequest());

        //BDD 검증
        then(reviewService).should(never()).createReviewLike(any(), any());
    }

    @Test
    @DisplayName("POST /api/reviews/{reviewId}/like - 리뷰 좋아요 실패(존재하지 않는 리뷰")
    void createReviewLike_fail_not_found() throws Exception {
        //BDD 모킹
        given(reviewService.createReviewLike(notFoundReviewId, requestUserId))
                .willThrow(new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

        //when & then
        mockMvc.perform(post("/api/reviews/{reviewId}/like", notFoundReviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isNotFound());

        //BDD 검증
        then(reviewService).should(times(1)).createReviewLike(notFoundReviewId, requestUserId);
    }

    @Test
    @DisplayName("GET /api/reviews/{reviewId} - 리뷰 상세 조회 조회 성공")
    void findDetailReview_success() throws Exception {
        //given
        ReviewDto reviewDto = createReviewDto(reviewId, bookId, userId);

        //BDD 모킹
        given(reviewService.findDetailReview(reviewId, requestUserId)).willReturn(reviewDto);

        //when & then
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewDto.id().toString()))
                .andExpect(jsonPath("$.content").value(reviewDto.content()));

        //BDD 검증
        then(reviewService).should().findDetailReview(reviewId, requestUserId);
    }

    @Test
    @DisplayName("GET /api/reviews/{reviewId} - 리뷰 상세 조회 실패(요청자 ID 누락)")
    void findDetailReview_fail_missing_header() throws Exception {
        //when & then
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isBadRequest());

        //BDD 검증
        then(reviewService).should(never()).findDetailReview(any(), any());
    }

    @Test
    @DisplayName("GET /api/reviews/{reviewId} - 리뷰 상세 조회 실패(존재하지 않는 리뷰)")
    void findDetailReview_fail_not_found() throws Exception {
        //BDD 모킹
        given(reviewService.findDetailReview(notFoundReviewId, requestUserId))
                .willThrow(new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

        //when & then
        mockMvc.perform(get("/api/reviews/{reviewId}", notFoundReviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isNotFound());

        //BDD 검증
        then(reviewService).should(times(1)).findDetailReview(notFoundReviewId, requestUserId);
    }

    @Test
    @DisplayName("DELETE: /api/reviews/{reviewId} - 리뷰 논리 삭제 성공")
    void softDeleteReview_success() throws Exception {
        //BDD 모킹
        willDoNothing().given(reviewService).softDeleteReview(reviewId, requestUserId);

        //when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isNoContent());

        //BDD 검증
        then(reviewService).should(times(1)).softDeleteReview(reviewId, requestUserId);
    }

    @Test
    @DisplayName("DELETE: /api/reviews/{reviewId} - 리뷰 논리 삭제 실패(요청자 ID 누락)")
    void softDeleteReview_fail_missing_header() throws Exception {
        //when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isBadRequest());

        //BDD 검증
        then(reviewService).should(never()).softDeleteReview(reviewId, requestUserId);
    }

    @Test
    @DisplayName("DELETE: /api/reviews/{reviewId} - 리뷰 논리 삭제 실패(리뷰 삭제 권한 없음)")
    void softDeleteReview_fail_forbidden() throws Exception {
        willThrow(new BusinessException(ReviewErrorCode.REVIEW_DELETE_PERMISSION_DENIED))
                .given(reviewService).softDeleteReview(reviewId, requestUserId);

        //when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isForbidden());

        //BDD 검증
        then(reviewService).should(times(1)).softDeleteReview(reviewId, requestUserId);
    }

    @Test
    @DisplayName("DELETE: /api/reviews/{reviewId} - 리뷰 논리 삭제 실패(존재하지 않는 리뷰)")
    void softDeleteReview_fail_not_found() throws Exception {
        //given
        // 반환값이 void일 경우 willThrow부터 사용
        //BDD 모킹
        willThrow(new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND))
                .given(reviewService).softDeleteReview(notFoundReviewId, requestUserId);

        //when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}", notFoundReviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isNotFound());

        //BDD 검증
        then(reviewService).should(times(1)).softDeleteReview(notFoundReviewId, requestUserId);
    }

    @Test
    @DisplayName("PATCH /api/reviews/{reviewId} - 리뷰 수정 성공")
    void updatedReview_success() throws Exception {
        //given
        ReviewUpdateRequest request = createReviewUpdateRequest(
                "test review",
                5
        );
        ReviewDto reviewDto = createReviewDto(reviewId, bookId, userId);

        //BDD 모킹
        given(reviewService.updateReview(eq(reviewId), any(ReviewUpdateRequest.class), eq(userId)))
                .willReturn(reviewDto);

        //when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                        .header("Deokhugam-Request-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewDto.id().toString()))
                .andExpect(jsonPath("$.rating").value(reviewDto.rating()))
                .andExpect(jsonPath("$.content").value(reviewDto.content()));

        //BDD 검증: 서비스의 update 메서드가 올바른 파라미터로 호출되었는 지 검증하는 확인할 수 있음
        then(reviewService).should().updateReview(
                eq(reviewId),
                argThat(req -> req.rating().equals(request.rating()) && req.content().equals(request.content())),
                eq(userId)
        );
    }

    @Test
    @DisplayName("PATCH /api/reviews/{reviewId} - 리뷰 수정 실패(요청자 ID 누락)")
    void updateReview_fail_missing_header() throws Exception {
        //when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isBadRequest());

        //BDD 검증
        then(reviewService).should(never()).updateReview(any(), any(), any());
    }

    @Test
    @DisplayName("PATCH /api/reviews/{reviewId} - 리뷰 수정 실패(존재하지 않는 리뷰)")
    void updateReview_fail_not_found() throws Exception {
        //given
        ReviewUpdateRequest request = createReviewUpdateRequest(
                "test review",
                5
        );

        //BDD 모킹
        given(reviewService.updateReview(eq(notFoundReviewId), any(ReviewUpdateRequest.class), eq(requestUserId)))
                .willThrow(new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

        //when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", notFoundReviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        //BDD 검증
        then(reviewService).should(times(1)).updateReview(eq(notFoundReviewId), any(ReviewUpdateRequest.class), eq(requestUserId));
    }

    @Test
    @DisplayName("PATCH /api/reviews/{reviewId} - 리뷰 수정 실패(권한이 없는 경우)")
    void updatedReview_fail_forbidden() throws Exception {
        //given
        ReviewUpdateRequest request = createReviewUpdateRequest(
                "update test review",
                2
        );

        //BDD 모킹
        given(reviewService.updateReview(eq(reviewId), any(ReviewUpdateRequest.class), eq(otherUserId)))
                .willThrow(new BusinessException(ReviewErrorCode.REVIEW_EDIT_PERMISSION_DENIED));

        //when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                        .header("Deokhugam-Request-User-ID", otherUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        //BDD 검증, 예외가 발생하였어도, 서비스 메서드에 올바른 파라미터가 들어갔는지 확인함
        then(reviewService).should().updateReview(
                eq(reviewId),
                argThat(req ->
                        req.rating().equals(request.rating()) && req.content().equals(request.content())),
                eq(otherUserId)
        );
    }

    @Test
    @DisplayName("DELETE /api/reviews/{reviewId} - 리뷰 물리 삭제 성공")
    void hardDeleteReview_success() throws Exception {
        //given
        //BDD 모킹
        willDoNothing().given(reviewService).hardDeleteReview(reviewId, requestUserId);

        //when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isNoContent());

        //BDD 검증
        then(reviewService).should(times(1)).hardDeleteReview(reviewId, requestUserId);
    }

    @Test
    @DisplayName("DELETE /api/reviews/{reviewId} - 리뷰 물리 삭제 실패(요청자 ID 누락)")
    void hardDeleteReview_fail_missing_header() throws Exception {
        //when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId))
                .andExpect(status().isBadRequest());

        //BDD 검증
        then(reviewService).should(never()).hardDeleteReview(reviewId, requestUserId);
    }

    @Test
    @DisplayName("DELETE /api/reviews/{reviewId} - 리뷰 물리 삭제 실패(리뷰 삭제 권한 없음)")
    void hardDeleteReview_fail_forbidden() throws Exception {
        willThrow(new BusinessException(ReviewErrorCode.REVIEW_DELETE_PERMISSION_DENIED))
                .given(reviewService).hardDeleteReview(reviewId, otherUserId);

        //when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
                        .header("Deokhugam-Request-User-ID", otherUserId.toString()))
                .andExpect(status().isForbidden());

        then(reviewService).should(times(1)).hardDeleteReview(reviewId, otherUserId);
    }

    @Test
    @DisplayName("DELETE /api/reviews/{reviewId} - 리뷰 물리 삭제 실패(존재하지 않는 리뷰)")
    void hardDeleteReview_fail_not_found() throws Exception {
        //given
        //BDD 모킹
        willThrow(new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND))
                .given(reviewService).hardDeleteReview(notFoundReviewId, requestUserId);

        //when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}/hard", notFoundReviewId)
                        .header("Deokhugam-Request-User-ID", requestUserId.toString()))
                .andExpect(status().isNotFound());

        //BDD 검증
        then(reviewService).should(times(1)).hardDeleteReview(notFoundReviewId, requestUserId);
    }

    private ReviewDto createReviewDto(UUID reviewId, UUID bookId, UUID userId) {
        return new ReviewDto(
                reviewId,
                bookId,
                "testBook",
                "https://test-thumbnail.url/image.jpg",
                userId,
                "testUser",
                "test review",
                5,
                0,
                0,
                false,
                Instant.now(),
                Instant.now()
        );
    }

    private ReviewCreateRequest createReviewCreateRequest(Integer rating) {
        return new ReviewCreateRequest(
                bookId,
                userId,
                "test review",
                rating
        );
    }

    private ReviewUpdateRequest createReviewUpdateRequest(String content, Integer rating) {
        return new ReviewUpdateRequest(
                content,
                rating
        );
    }

    private ReviewPageRequest createReviewPageRequest(
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            String after,
            Integer limit
    ) {
        return new ReviewPageRequest(
                userId,
                bookId,
                keyword,
                orderBy,
                direction,
                cursor,
                after,
                limit,
                requestUserId
        );
    }

    private MultiValueMap<String, String> toMultiValueMap(Object obj) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        // ObjectMapper를 이용해 객체를 map으로 전환
        Map<String, Object> maps = objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
        });

        maps.forEach((key, value) -> {
            if (value != null) {
                parameters.add(key, String.valueOf(value));
            }
        });
        return parameters;
    }
}
