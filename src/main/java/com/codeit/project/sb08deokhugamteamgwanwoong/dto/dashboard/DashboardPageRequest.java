package com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;

public record DashboardPageRequest(
		DashboardPeriodEnums period,
		String direction,
		String cursor,
		String after,
		Integer limit
) {
	public DashboardPageRequest {
		if (limit == null || limit <= 0) {
			limit = 50;
		}
		if (period == null) {
			period = DashboardPeriodEnums.DAILY;
		}
		if (direction == null || direction.isBlank()) {
			direction = "ASC"; //대시보드는 1위부터 보여주기 위해 ASC 사용
		}
	}
}
