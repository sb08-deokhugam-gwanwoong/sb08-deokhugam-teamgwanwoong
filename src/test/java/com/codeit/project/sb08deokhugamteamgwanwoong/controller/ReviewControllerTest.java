package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.support.ControllerTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReviewControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("POST /api/reviews - 리뷰 생성 성공")
    void createReview_success() throws Exception {
        //given
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                "test review",
                5
        );

        ReviewDto reviewDto = createReviewDto(reviewId, bookId, userId);

        //BDD 모킹
        given(reviewService.createReview(any(ReviewCreateRequest.class))).willReturn(reviewDto);

        //when & then
        mockMvc.perform(post("/api/reviews")
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
        ReviewCreateRequest request = new ReviewCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test review",
                6
        );

        //when & then
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(reviewService).should(never()).createReview(any());
    }

    @Test
    @DisplayName("PATCH /api/reviews/{reviewId} - 리뷰 수정 성공")
    void updatedReview_success() throws Exception {
        //given
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewUpdateRequest request = new ReviewUpdateRequest(
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
    @DisplayName("PATCH /api/reviews/{reviewId} - 리뷰 수정 실패(권한이 없는 경우)")
    void updatedReview_fail_forbidden() throws Exception {
        //given
        UUID differentUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "update test review",
                2
        );

        //BDD 모킹
        given(reviewService.updateReview(eq(reviewId), any(ReviewUpdateRequest.class), eq(differentUserId)))
                .willThrow(new BusinessException(ReviewErrorCode.REVIEW_EDIT_PERMISSION_DENIED));

        //when & then
        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                        .header("Deokhugam-Request-User-ID", differentUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        //BDD 검증, 예외가 발생하였어도, 서비스 메서드에 올바른 파라미터가 들어갔는지 확인함
        then(reviewService).should().updateReview(
                eq(reviewId),
                argThat(req ->
                        req.rating().equals(request.rating()) && req.content().equals(request.content())),
                eq(differentUserId)
        );
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
}
