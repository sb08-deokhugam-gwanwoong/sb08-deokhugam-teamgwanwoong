package com.codeit.project.sb08deokhugamteamgwanwoong.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.project.sb08deokhugamteamgwanwoong.component.batch.DashboardBatchService;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardTargetType;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.DashboardRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 배치 서비스")
class DashboardBatchServiceTest {

	@Mock
	private DashboardRepository dashboardRepository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private NotificationService notificationService;

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
	@DisplayName("refreshPopularReviews: delete 후 insertPopularReviews가 호출되고, 10위 내 선정 시 알림 생성이 시도된다.")
	void refreshPopularReviews_callsDeleteAndInsert() {
		// Given
		when(dashboardRepository.insertPopularReviews(any(Instant.class), eq("WEEKLY"))).thenReturn(5);
		when(dashboardRepository.findTop10ByTargetTypeAndPeriodTypeOrderByRankingPosAsc(
				DashboardTargetType.REVIEW, DashboardPeriodEnums.WEEKLY)).thenReturn(Collections.emptyList());

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

	@Nested
	@DisplayName("notifyTop10PopularReviews (인기 리뷰 10위 내 알림)")
	class NotifyTop10PopularReviews {

		@Test
		@DisplayName("10위 내 리뷰가 있으면 해당 작성자에게 알림이 생성된다.")
		void withTop10_createsNotification() {
			// Given
			UUID reviewId = UUID.randomUUID();
			User user = mock(User.class);
			when(user.getNickname()).thenReturn("테스트유저");
			Review review = mock(Review.class);
			when(review.getId()).thenReturn(reviewId);
			when(review.getUser()).thenReturn(user);

			Dashboard dashboard = Dashboard.builder()
					.targetId(reviewId)
					.targetType(DashboardTargetType.REVIEW)
					.periodType(DashboardPeriodEnums.DAILY)
					.score(100.0)
					.rankingPos(1)
					.build();

			@SuppressWarnings("unchecked")
			TypedQuery<Review> typedQuery = mock(TypedQuery.class);

			when(dashboardRepository.insertPopularReviews(any(Instant.class), eq("DAILY"))).thenReturn(5);
			when(dashboardRepository.findTop10ByTargetTypeAndPeriodTypeOrderByRankingPosAsc(
					eq(DashboardTargetType.REVIEW), eq(DashboardPeriodEnums.DAILY))).thenReturn(List.of(dashboard));
			when(entityManager.createQuery(anyString(), eq(Review.class))).thenReturn(typedQuery);
			when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
			when(typedQuery.getResultList()).thenReturn(List.of(review));

			// When
			dashboardBatchService.refreshPopularReviews(DashboardPeriodEnums.DAILY);

			// Then
			ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
			verify(notificationService).createNotification(eq(user), eq(review), messageCaptor.capture());
			assertThat(messageCaptor.getValue()).isEqualTo("[테스트유저]님의 리뷰가 일간 인기 리뷰 1위에 선정되었습니다!");
		}

		@ParameterizedTest
		@EnumSource(DashboardPeriodEnums.class)
		@DisplayName("각 기간별 period label이 올바르게 적용된다.")
		void periodLabel_periodType(DashboardPeriodEnums period) {
			// Given
			String expectedLabel = switch (period) {
				case DAILY -> "일간";
				case WEEKLY -> "주간";
				case MONTHLY -> "월간";
				case ALL_TIME -> "전체";
			};

			UUID reviewId = UUID.randomUUID();
			User user = mock(User.class);
			when(user.getNickname()).thenReturn("닉네임");
			Review review = mock(Review.class);
			when(review.getId()).thenReturn(reviewId);
			when(review.getUser()).thenReturn(user);

			Dashboard dashboard = Dashboard.builder()
					.targetId(reviewId)
					.targetType(DashboardTargetType.REVIEW)
					.periodType(period)
					.score(80.0)
					.rankingPos(2)
					.build();

			@SuppressWarnings("unchecked")
			TypedQuery<Review> typedQuery = mock(TypedQuery.class);

			when(dashboardRepository.insertPopularReviews(any(Instant.class), eq(period.name()))).thenReturn(5);
			when(dashboardRepository.findTop10ByTargetTypeAndPeriodTypeOrderByRankingPosAsc(
					eq(DashboardTargetType.REVIEW), eq(period))).thenReturn(List.of(dashboard));
			when(entityManager.createQuery(anyString(), eq(Review.class))).thenReturn(typedQuery);
			when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
			when(typedQuery.getResultList()).thenReturn(List.of(review));

			// When
			dashboardBatchService.refreshPopularReviews(period);

			// Then
			ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
			verify(notificationService).createNotification(eq(user), eq(review), messageCaptor.capture());
			assertThat(messageCaptor.getValue()).contains(expectedLabel);
		}

		@Test
		@DisplayName("reviewMap에 없는 리뷰(삭제됨)는 알림을 생성하지 않고 건너뛴다.")
		void reviewNotFound_skipsNotification() {
			// Given: Dashboard에 있으나 조회 결과에 없는 리뷰 (삭제된 경우)
			UUID deletedReviewId = UUID.randomUUID();
			Dashboard dashboard = Dashboard.builder()
					.targetId(deletedReviewId)
					.targetType(DashboardTargetType.REVIEW)
					.periodType(DashboardPeriodEnums.WEEKLY)
					.score(90.0)
					.rankingPos(1)
					.build();

			@SuppressWarnings("unchecked")
			TypedQuery<Review> typedQuery = mock(TypedQuery.class);

			when(dashboardRepository.insertPopularReviews(any(Instant.class), eq("WEEKLY"))).thenReturn(5);
			when(dashboardRepository.findTop10ByTargetTypeAndPeriodTypeOrderByRankingPosAsc(
					eq(DashboardTargetType.REVIEW), eq(DashboardPeriodEnums.WEEKLY))).thenReturn(List.of(dashboard));
			when(entityManager.createQuery(anyString(), eq(Review.class))).thenReturn(typedQuery);
			when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
			when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

			// When
			dashboardBatchService.refreshPopularReviews(DashboardPeriodEnums.WEEKLY);

			// Then
			verify(notificationService, never()).createNotification(any(), any(), anyString());
		}
	}
}
