package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.integration.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DashboardServiceTest extends IntegrationTestSupport {

		@Autowired
		private DashboardService dashboardService;

		@DisplayName("리뷰 수와 평점을 계산하여 인기 도서 목록을 점수 순으로 조회한다.")
		@Test
		void getPopularBooks() {
				// given: 테스트에 필요한 데이터(Book, Review)가 DB에 있다고 가정합니다.
				// 이 부분은 나중에 Repository를 주입받아 데이터를 직접 세팅할 예정입니다.

				// when
				List<PopularBookDto> response = dashboardService.getPopularBooks();

				// then: 점수 공식 (리뷰수 * 0.4 + 평점 * 0.6)에 의해 정렬되었는지 검증할 예정입니다.
				assertThat(response).isEmpty();
		}
}