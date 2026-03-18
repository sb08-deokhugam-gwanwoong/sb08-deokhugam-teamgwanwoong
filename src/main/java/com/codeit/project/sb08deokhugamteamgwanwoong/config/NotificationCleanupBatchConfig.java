package com.codeit.project.sb08deokhugamteamgwanwoong.config;

import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class NotificationCleanupBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final NotificationService notificationService;

  @Bean
  public Job cleanupJob() {
    return new JobBuilder("cleanupNotificationJob", jobRepository)
        .start(cleanupStep())
        .build();
  }

  @Bean
  public Step cleanupStep() {
    return new StepBuilder("cleanupNotificationStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          notificationService.deleteOldNotifications();
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }
}
