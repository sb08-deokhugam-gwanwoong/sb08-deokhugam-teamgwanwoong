package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

import java.util.UUID;

public record ReviewLikeDto(
        UUID reviewId,
        UUID userId,
        boolean liked
) {
}
