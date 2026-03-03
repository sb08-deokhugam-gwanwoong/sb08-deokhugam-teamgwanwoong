package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "리뷰 생성 정보")
public record ReviewCreateRequest(
        @NotNull(message = "책 아이디가 필요합니다.")
        Long bookId,

        @NotNull(message = "유저 아이디가 필요합니다.")
        Long userId,

        @NotBlank(message = "리뷰 내용은 필수 정보입니다.")
        String content,

        @NotNull(message = "평점은 필수 정보입니다.")
        @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
        Integer rating
) {
}
