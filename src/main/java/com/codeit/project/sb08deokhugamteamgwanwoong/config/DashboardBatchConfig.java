package com.codeit.project.sb08deokhugamteamgwanwoong.config;

import com.codeit.project.sb08deokhugamteamgwanwoong.component.batch.DashboardBatchService;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 대시보드 랭킹 갱신 Spring Batch Job 설정.
 * <p>트랜잭션: Step은 Batch의 PlatformTransactionManager로 커밋되며,
 * 서비스 메서드의 @Transactional(REQUIRED)는 동일 트랜잭션에 참여합니다.
 * 현재 로직은 Native Query 위주라 준영속(Detached) 이슈는 없습니다.</p>
 */
@Configuration
public class DashboardBatchConfig {

	private static final String JOB_NAME = "dashboardRefreshJob";
	private static final String STEP_BOOKS = "refreshPopularBooksStep";
	private static final String STEP_REVIEWS = "refreshPopularReviewsStep";
	private static final String STEP_POWER_USERS = "refreshPowerUsersStep";

	/**
	 * 대시보드 전체 랭킹을 갱신하는 Job 정의 (도서 → 리뷰 → 파워 유저 순서 실행).
	 */
	@Bean
	public Job dashboardRefreshJob(
			JobRepository jobRepository,
			Step refreshPopularBooksStep,
			Step refreshPopularReviewsStep,
			Step refreshPowerUsersStep
	) {
		return new JobBuilder(JOB_NAME, jobRepository)
				.start(refreshPopularBooksStep)
				.next(refreshPopularReviewsStep)
				.next(refreshPowerUsersStep)
				.build();
	}

	/**
	 * 인기 도서 랭킹을 기간별(일간/주간/월간/전체)로 갱신하는 Step.
	 * 실제 비즈니스 로직은 DashboardBatchService.refreshPopularBooks 에 위임합니다.
	 */
	@Bean
	public Step refreshPopularBooksStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			DashboardBatchService dashboardBatchService
	) {
		Tasklet tasklet = (contribution, chunkContext) -> {
			for (DashboardPeriodEnums period : DashboardPeriodEnums.values()) {
				dashboardBatchService.refreshPopularBooks(period);
			}
			return RepeatStatus.FINISHED;
		};
		return new StepBuilder(STEP_BOOKS, jobRepository)
				.tasklet(tasklet, transactionManager)
				.build();
	}

	/**
	 * 인기 리뷰 랭킹을 기간별로 갱신하고, 10위 내 리뷰에 대한 알림 생성까지 수행하는 Step.
	 */
	@Bean
	public Step refreshPopularReviewsStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			DashboardBatchService dashboardBatchService
	) {
		Tasklet tasklet = (contribution, chunkContext) -> {
			for (DashboardPeriodEnums period : DashboardPeriodEnums.values()) {
				dashboardBatchService.refreshPopularReviews(period);
			}
			return RepeatStatus.FINISHED;
		};
		return new StepBuilder(STEP_REVIEWS, jobRepository)
				.tasklet(tasklet, transactionManager)
				.build();
	}

	/**
	 * 파워 유저 랭킹을 기간별로 갱신하는 Step.
	 */
	@Bean
	public Step refreshPowerUsersStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			DashboardBatchService dashboardBatchService
	) {
		Tasklet tasklet = (contribution, chunkContext) -> {
			for (DashboardPeriodEnums period : DashboardPeriodEnums.values()) {
				dashboardBatchService.refreshPowerUsers(period);
			}
			return RepeatStatus.FINISHED;
		};
		return new StepBuilder(STEP_POWER_USERS, jobRepository)
				.tasklet(tasklet, transactionManager)
				.build();
	}
}
