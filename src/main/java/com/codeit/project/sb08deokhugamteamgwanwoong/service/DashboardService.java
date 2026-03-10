package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePowerUserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.DashboardPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PowerUserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import java.util.List;

public interface DashboardService {
	List<PopularBookDto> getPopularBooks();

	List<PopularReviewDto> getPopularReviews(DashboardPeriodEnums period);

	List<PowerUserDto> getPowerUsers(DashboardPeriodEnums period);

	CursorPageResponsePopularBookDto getPopularBooks(DashboardPageRequest request);

	CursorPageResponsePopularReviewDto getPopularReviews(DashboardPageRequest request);

	CursorPageResponsePowerUserDto getPowerUsers(DashboardPageRequest request);
}
