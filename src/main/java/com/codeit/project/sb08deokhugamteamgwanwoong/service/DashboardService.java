package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PowerUserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import java.util.List;

public interface DashboardService {
		List<PopularBookDto> getPopularBooks();

		List<PopularReviewDto> getPopularReviews(DashboardPeriodEnums period);

		List<PowerUserDto> getPowerUsers(DashboardPeriodEnums period);
}
