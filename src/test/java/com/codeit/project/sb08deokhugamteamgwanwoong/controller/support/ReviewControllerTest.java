package com.codeit.project.sb08deokhugamteamgwanwoong.controller.support;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReviewControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("POST /api/reviews - 리뷰 생성 성공")
    void createReview_success() throws Exception {
        //given
        ReviewCreateRequest request = new ReviewCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test review",
                5
        );
        ReviewDto reviewDto = new ReviewDto(
                UUID.randomUUID(),
                request.bookId(),
                "testBook",
                "https://test-thumbnail.url/image.jpg",
                request.userId(),
                "testUser",
                request.content(),
                request.rating(),
                0,
                0,
                false,
                Instant.now(),
                Instant.now()
        );

        //BDD 모킹
        given(reviewService.create(any(ReviewCreateRequest.class))).willReturn(reviewDto);

        //when & then
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reviewDto.id().toString()))
                .andExpect(jsonPath("$.rating").value(reviewDto.rating()))
                .andExpect(jsonPath("$.content").value(reviewDto.content()));

        //BDD 검증
        then(reviewService).should().create(argThat(req ->
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

        then(reviewService).should(never()).create(any());
    }
}
