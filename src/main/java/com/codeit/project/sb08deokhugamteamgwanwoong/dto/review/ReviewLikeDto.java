package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

import java.util.UUID;

public record ReviewLikeDto(
        Long reviewId,
        Long userId,
        boolean liked
) {
}
