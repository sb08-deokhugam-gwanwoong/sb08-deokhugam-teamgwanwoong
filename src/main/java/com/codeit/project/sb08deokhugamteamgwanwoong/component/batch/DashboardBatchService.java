package com.codeit.project.sb08deokhugamteamgwanwoong.component.batch;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardTargetType;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.DashboardRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardBatchService {

	private final DashboardRepository dashboardRepository;

	/** 기간에 따른 조회 시작 시점 */
	private static Instant getSince(DashboardPeriodEnums period) {
		return switch (period) {
			case DAILY -> Instant.now().minus(Duration.ofDays(1));
			case WEEKLY -> Instant.now().minus(Duration.ofDays(7));
			case MONTHLY -> Instant.now().minus(Duration.ofDays(30));
			case ALL_TIME -> Instant.EPOCH;
		};
	}

	/** 인기 도서 랭킹 갱신 (점수 = 리뷰수×0.4 + 평균평점×0.6, SQL Window Function으로 ranking_pos 계산) */
	@Transactional
	public void refreshPopularBooks(DashboardPeriodEnums period) {
		Instant since = getSince(period);
		dashboardRepository.deleteByTargetTypeAndPeriodType(DashboardTargetType.BOOK, period);
		int inserted = dashboardRepository.insertPopularBooks(since, period.name());
		log.info("[대시보드 배치] 인기 도서 {} 랭킹 {}건 반영 완료", period, inserted);
	}

	/** 인기 리뷰 랭킹 갱신 (점수 = 좋아요수×0.3 + 댓글수×0.7, SQL Window Function으로 ranking_pos 계산) */
	@Transactional
	public void refreshPopularReviews(DashboardPeriodEnums period) {
		Instant since = getSince(period);
		dashboardRepository.deleteByTargetTypeAndPeriodType(DashboardTargetType.REVIEW, period);
		int inserted = dashboardRepository.insertPopularReviews(since, period.name());
		log.info("[대시보드 배치] 인기 리뷰 {} 랭킹 {}건 반영 완료", period, inserted);
	}

	/** 파워 유저 랭킹 갱신 (점수 = 리뷰인기점수합×0.5 + 좋아요×0.2 + 댓글수×0.3, SQL Window Function으로 ranking_pos 계산) */
	@Transactional
	public void refreshPowerUsers(DashboardPeriodEnums period) {
		Instant since = getSince(period);
		dashboardRepository.deleteByTargetTypeAndPeriodType(DashboardTargetType.USER, period);
		int inserted = dashboardRepository.insertPowerUsers(since, period.name());
		log.info("[대시보드 배치] 파워 유저 {} 랭킹 {}건 반영 완료", period, inserted);
	}
}
