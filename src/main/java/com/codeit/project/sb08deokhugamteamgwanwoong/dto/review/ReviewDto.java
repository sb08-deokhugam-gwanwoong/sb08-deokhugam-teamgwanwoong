package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

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
        String createdAt,
        String updatedAt
) {
}
