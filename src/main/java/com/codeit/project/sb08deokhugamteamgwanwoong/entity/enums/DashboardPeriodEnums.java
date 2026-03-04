package com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DashboardPeriodEnums {
		DAILY("일간"),
		WEEKLY("주간"),
		MONTHLY("월간"),
		ALL_TIME("전체");

		private final String description;
}