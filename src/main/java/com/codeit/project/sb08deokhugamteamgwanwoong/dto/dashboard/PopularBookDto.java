package com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PopularBookDto(
		UUID id,
		UUID bookId,
		String title,
		String author,
		String thumbnailUrl,
		String period,
		Long rank,
		Double score,
		Long reviewCount,
		Double rating,
		Instant createdAt
) {
}
