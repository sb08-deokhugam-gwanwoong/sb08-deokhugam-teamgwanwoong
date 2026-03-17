package com.codeit.project.sb08deokhugamteamgwanwoong.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.component.batch.DashboardBatchScheduler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class DashboardBatchSchedulerTest {

	private static final String JOB_NAME = "dashboardRefreshJob";

	@Mock
	private JobLauncher jobLauncher;

	@Mock
	private Job dashboardRefreshJob;

	private MeterRegistry meterRegistry;

	private DashboardBatchScheduler dashboardBatchScheduler;

	@BeforeEach
	void setUp() {
		meterRegistry = new SimpleMeterRegistry();
		dashboardBatchScheduler = new DashboardBatchScheduler(jobLauncher, dashboardRefreshJob, meterRegistry);
	}

	@Test
	@DisplayName("대시보드 배치 스케줄러 성공: JobLauncher를 통해 Batch Job이 실행되고 성공 메트릭이 증가한다.")
	void refreshDashboardRankings_Success() throws Exception {
		// Given
		JobExecution execution = new JobExecution(1L);
		org.mockito.Mockito.when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
				.thenReturn(execution);

		// When
		dashboardBatchScheduler.refreshDashboardRankings();

		// Then
		verify(jobLauncher).run(any(Job.class), any(JobParameters.class));
		double successCount = meterRegistry
				.get("dashboard.batch.job.success.count")
				.tag("job", JOB_NAME)
				.counter()
				.count();
		assertThat(successCount).isEqualTo(1.0d);
	}

	@Test
	@DisplayName("대시보드 배치 스케줄러 실패: Job 실행 중 예외 발생 시 실패 메트릭이 증가하고 예외는 밖으로 전파되지 않는다.")
	void refreshDashboardRankings_ExceptionHandling() throws Exception {
		// Given
		doThrow(new RuntimeException("Error"))
				.when(jobLauncher).run(any(Job.class), any(JobParameters.class));

		// When - 내부 try-catch 때문에 예외가 밖으로 터지지 않는다.
		dashboardBatchScheduler.refreshDashboardRankings();

		// Then
		verify(jobLauncher).run(any(Job.class), any(JobParameters.class));
		double failureCount = meterRegistry
				.get("dashboard.batch.job.failure.count")
				.tag("job", JOB_NAME)
				.counter()
				.count();
		assertThat(failureCount).isEqualTo(1.0d);
	}
}
