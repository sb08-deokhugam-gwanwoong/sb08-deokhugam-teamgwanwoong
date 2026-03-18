package com.codeit.project.sb08deokhugamteamgwanwoong.service.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.component.batch.NotificationBatchScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
public class NotificationBatchSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job cleanupJob;

  @InjectMocks
  private NotificationBatchScheduler notificationBatchScheduler;

  @Test
  @DisplayName("알림 삭제 배치 실행 성공: JobLauncher가 정상적으로 호출되어야 한다.")
  void runCleanupJob_Test() throws Exception {
    // When
    notificationBatchScheduler.runCleanupJob();

    // Then - jobLauncher.run이 호출되었는지 검증
    verify(jobLauncher).run(any(Job.class), any(JobParameters.class));
  }
}
