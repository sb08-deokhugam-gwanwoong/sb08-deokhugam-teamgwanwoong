package com.codeit.project.sb08deokhugamteamgwanwoong.component.batch;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 대시보드 랭킹 배치 Job 실행 스케줄러.
 * <p>Spring Batch Job을 호출하고, 실행 결과를 Micrometer 커스텀 메트릭으로 기록합니다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardBatchScheduler {

	private static final String JOB_NAME = "dashboardRefreshJob";

	private final JobLauncher jobLauncher;
	private final Job dashboardRefreshJob;
	private final MeterRegistry meterRegistry;

	/** 매일 새벽 1시(Asia/Seoul)에 대시보드 랭킹 배치 Job 실행 및 메트릭 수집. */
	@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
	public void refreshDashboardRankings() {
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			log.info("[대시보드 배치] 랭킹 갱신 Job 시작");

			JobParameters jobParameters = new JobParametersBuilder()
					.addLong("run.id", System.currentTimeMillis())
					.toJobParameters();

			JobExecution execution = jobLauncher.run(dashboardRefreshJob, jobParameters);
			log.info("[대시보드 배치] 랭킹 갱신 Job 종료 - status: {}", execution.getStatus());

			meterRegistry.counter("dashboard.batch.job.success.count",
							"job", JOB_NAME)
					.increment();
		} catch (Exception e) {
			log.error("[대시보드 배치] 랭킹 갱신 Job 실행 중 예외 발생: {}", e.getMessage(), e);
			meterRegistry.counter("dashboard.batch.job.failure.count",
							"job", JOB_NAME)
					.increment();
		} finally {
			sample.stop(meterRegistry.timer("dashboard.batch.job.duration",
					"job", JOB_NAME));
		}
	}
}
