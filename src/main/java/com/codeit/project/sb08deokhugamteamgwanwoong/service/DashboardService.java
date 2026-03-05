package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import java.util.List;

public interface DashboardService {
		List<PopularBookDto> getPopularBooks();
}
