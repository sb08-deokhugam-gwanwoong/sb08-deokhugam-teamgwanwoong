package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

import java.util.List;

public record CursorPageResponseReviewDto(
        List<ReviewDto> content,
        String nextCursor,
        String nextAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {
}
