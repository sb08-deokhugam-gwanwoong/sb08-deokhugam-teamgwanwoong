package com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import java.time.Instant;
import java.util.UUID;

public record PowerUserDto(
		UUID userId,
		String nickname,
		DashboardPeriodEnums period,
		Instant createdAt,
		Long rank,
		Double score,
		Double reviewScoreSum,
		Long likeCount,
		Long commentCount
) {
}
