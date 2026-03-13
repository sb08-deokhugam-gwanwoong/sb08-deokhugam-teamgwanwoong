package com.codeit.project.sb08deokhugamteamgwanwoong.component.batch;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardTargetType;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.DashboardRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardBatchService {

	private final DashboardRepository dashboardRepository;
	private final EntityManager entityManager;
	private final NotificationService notificationService;

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
		notifyTop10PopularReviews(period);
		log.info("[대시보드 배치] 인기 리뷰 {} 랭킹 {}건 반영 완료", period, inserted);
	}

	/** 각 기간별 10위 내 인기 리뷰 선정 시 리뷰 작성자에게 알림 생성 (Fetch Join으로 N+1 방지) */
	private void notifyTop10PopularReviews(DashboardPeriodEnums period) {
		List<Dashboard> top10 = dashboardRepository.findTop10ByTargetTypeAndPeriodTypeOrderByRankingPosAsc(
				DashboardTargetType.REVIEW, period);
		if (top10.isEmpty()) {
			return;
		}

		List<UUID> reviewIds = top10.stream().map(Dashboard::getTargetId).toList();
		List<Review> reviews = entityManager.createQuery(
				"SELECT r FROM Review r JOIN FETCH r.user WHERE r.id IN :ids", Review.class)
				.setParameter("ids", reviewIds)
				.getResultList();

		Map<UUID, Review> reviewMap = reviews.stream().collect(Collectors.toMap(Review::getId, Function.identity()));
		String periodLabel = getPeriodLabel(period);

		for (Dashboard d : top10) {
			Review review = reviewMap.get(d.getTargetId());
			if (review == null) {
				continue;
			}
			User toUser = review.getUser();
			String message = String.format("[%s]님의 리뷰가 %s 인기 리뷰 %d위에 선정되었습니다!", toUser.getNickname(), periodLabel, d.getRankingPos());
			notificationService.createNotification(toUser, review, message);
		}
	}

	private static String getPeriodLabel(DashboardPeriodEnums period) {
		return switch (period) {
			case DAILY -> "일간";
			case WEEKLY -> "주간";
			case MONTHLY -> "월간";
			case ALL_TIME -> "전체";
		};
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
