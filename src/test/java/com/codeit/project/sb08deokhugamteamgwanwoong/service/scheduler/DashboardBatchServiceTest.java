package com.codeit.project.sb08deokhugamteamgwanwoong.service.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.project.sb08deokhugamteamgwanwoong.component.batch.DashboardBatchService;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardTargetType;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.DashboardRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 배치 서비스")
class DashboardBatchServiceTest {

	@Mock
	private DashboardRepository dashboardRepository;

	@InjectMocks
	private DashboardBatchService dashboardBatchService;

	@Test
	@DisplayName("refreshPopularBooks: delete 후 insertPopularBooks가 호출된다.")
	void refreshPopularBooks_callsDeleteAndInsert() {
		// Given
		when(dashboardRepository.insertPopularBooks(any(Instant.class), eq("DAILY"))).thenReturn(10);

		// When
		dashboardBatchService.refreshPopularBooks(DashboardPeriodEnums.DAILY);

		// Then
		verify(dashboardRepository).deleteByTargetTypeAndPeriodType(DashboardTargetType.BOOK, DashboardPeriodEnums.DAILY);
		verify(dashboardRepository).insertPopularBooks(any(Instant.class), eq("DAILY"));
	}

	@Test
	@DisplayName("refreshPopularBooks: ALL_TIME period 시 since는 EPOCH로 계산된다.")
	void refreshPopularBooks_allTime_period() {
		// Given
		when(dashboardRepository.insertPopularBooks(any(Instant.class), eq("ALL_TIME"))).thenReturn(0);

		// When
		dashboardBatchService.refreshPopularBooks(DashboardPeriodEnums.ALL_TIME);

		// Then
		verify(dashboardRepository).deleteByTargetTypeAndPeriodType(DashboardTargetType.BOOK, DashboardPeriodEnums.ALL_TIME);
		verify(dashboardRepository).insertPopularBooks(any(Instant.class), eq("ALL_TIME"));
	}

	@Test
	@DisplayName("refreshPopularReviews: delete 후 insertPopularReviews가 호출된다.")
	void refreshPopularReviews_callsDeleteAndInsert() {
		// Given
		when(dashboardRepository.insertPopularReviews(any(Instant.class), eq("WEEKLY"))).thenReturn(5);

		// When
		dashboardBatchService.refreshPopularReviews(DashboardPeriodEnums.WEEKLY);

		// Then
		verify(dashboardRepository).deleteByTargetTypeAndPeriodType(DashboardTargetType.REVIEW, DashboardPeriodEnums.WEEKLY);
		verify(dashboardRepository).insertPopularReviews(any(Instant.class), eq("WEEKLY"));
	}

	@Test
	@DisplayName("refreshPowerUsers: delete 후 insertPowerUsers가 호출된다.")
	void refreshPowerUsers_callsDeleteAndInsert() {
		// Given
		when(dashboardRepository.insertPowerUsers(any(Instant.class), eq("MONTHLY"))).thenReturn(20);

		// When
		dashboardBatchService.refreshPowerUsers(DashboardPeriodEnums.MONTHLY);

		// Then
		verify(dashboardRepository).deleteByTargetTypeAndPeriodType(DashboardTargetType.USER, DashboardPeriodEnums.MONTHLY);
		verify(dashboardRepository).insertPowerUsers(any(Instant.class), eq("MONTHLY"));
	}
}
