package com.codeit.project.sb08deokhugamteamgwanwoong.service.scheduler;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.component.batch.DashboardBatchScheduler;
import com.codeit.project.sb08deokhugamteamgwanwoong.component.batch.DashboardBatchService;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardBatchSchedulerTest {

	@Mock
	private DashboardBatchService dashboardBatchService;

	@InjectMocks
	private DashboardBatchScheduler dashboardBatchScheduler;

	@Test
	@DisplayName("대시보드 배치 스케줄러 성공: 모든 period에 대해 인기 도서/리뷰/파워유저 랭킹 갱신이 호출된다.")
	void refreshDashboardRankings_Success() {
		// When
		dashboardBatchScheduler.refreshDashboardRankings();

		// Then - DAILY, WEEKLY, MONTHLY, ALL_TIME 각 period별로 1번씩 호출
		for (DashboardPeriodEnums period : DashboardPeriodEnums.values()) {
			verify(dashboardBatchService).refreshPopularBooks(period);
			verify(dashboardBatchService).refreshPopularReviews(period);
			verify(dashboardBatchService).refreshPowerUsers(period);
		}
	}

	@Test
	@DisplayName("대시보드 배치 스케줄러 실패: 서비스 호출 중 예외 발생 시 catch 블록에서 처리되어야 한다.")
	void refreshDashboardRankings_ExceptionHandling() {
		// Given
		doThrow(new RuntimeException("Error"))
				.when(dashboardBatchService).refreshPopularBooks(DashboardPeriodEnums.DAILY);

		// When - 내부 try-catch 때문에 예외가 밖으로 터지지 않는다.
		dashboardBatchScheduler.refreshDashboardRankings();

		// Then - 첫 호출에서 예외가 발생했어도 호출 시도가 있었는지 검증
		verify(dashboardBatchService).refreshPopularBooks(DashboardPeriodEnums.DAILY);
	}
}
