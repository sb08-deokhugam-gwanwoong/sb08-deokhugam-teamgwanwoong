package com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PopularBookDto(
		UUID id,
		UUID bookId,
		String title,
		String author,
		String thumbnailUrl,
		DashboardPeriodEnums period,
		Long rank,
		Double score,
		Long reviewCount,
		Double rating,
		Instant createdAt
) {
}
