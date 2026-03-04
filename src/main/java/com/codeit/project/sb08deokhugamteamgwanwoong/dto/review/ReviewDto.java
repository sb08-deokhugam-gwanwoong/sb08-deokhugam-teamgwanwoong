package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
        UUID id,

        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,

        UUID userId,
        String userNickname,

        String content,
        Integer rating,
        Integer likeCount,
        Integer commentCount,
        boolean likedByMe,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy. M. d.", timezone = "Asia/Seoul")
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy. M. d.", timezone = "Asia/Seoul")
        Instant updatedAt
) {
}
