package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

public record ReviewDto(
        Long id,

        Long bookId,
        String bookTitle,
        String bookThumbnailUrl,

        Long userId,
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
