package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;


import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.DashboardService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

		@Override
		public List<PopularBookDto> getPopularBooks() {
				// 일단 테스트가 '컴파일'만 되게 빈 리스트를 반환합니다.
				return List.of();
		}
}

