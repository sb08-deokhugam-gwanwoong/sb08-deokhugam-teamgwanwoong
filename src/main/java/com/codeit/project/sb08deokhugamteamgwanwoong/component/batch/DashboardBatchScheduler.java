package com.codeit.project.sb08deokhugamteamgwanwoong.component.batch;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardBatchScheduler {

	private final DashboardBatchService dashboardBatchService;

	/** 매일 새벽 1시(Asia/Seoul)에 대시보드 랭킹 갱신 */
	@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
	public void refreshDashboardRankings() {
		try {
			log.info("[대시보드 배치] 랭킹 갱신 시작");

			for (DashboardPeriodEnums period : DashboardPeriodEnums.values()) {
				dashboardBatchService.refreshPopularBooks(period);
				dashboardBatchService.refreshPopularReviews(period);
				dashboardBatchService.refreshPowerUsers(period);
			}

			log.info("[대시보드 배치] 랭킹 갱신 완료");
		} catch (Exception e) {
			log.error("[대시보드 배치] 랭킹 갱신 중 예외 발생: {}", e.getMessage(), e);
		}
	}
}
