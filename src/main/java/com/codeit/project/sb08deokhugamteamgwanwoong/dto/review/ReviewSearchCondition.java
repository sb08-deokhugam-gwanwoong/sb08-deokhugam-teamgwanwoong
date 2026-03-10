package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

import lombok.Builder;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ReviewSearchCondition(
        UUID userId,
        UUID bookId,
        String keyword,
        String cursor,
        Instant after,
        String orderBy,
        Sort.Direction direction
) {
}
