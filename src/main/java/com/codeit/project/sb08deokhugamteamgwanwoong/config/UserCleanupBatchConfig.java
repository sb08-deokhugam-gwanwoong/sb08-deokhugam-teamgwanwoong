package com.codeit.project.sb08deokhugamteamgwanwoong.config;

import com.codeit.project.sb08deokhugamteamgwanwoong.service.UserService;
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
public class UserCleanupBatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final UserService userService;

  @Bean
  public Job cleanupUserJob() {
    return new JobBuilder("cleanupUserJob", jobRepository)
        .start(cleanupUserStep())
        .build();
  }

  @Bean
  public Step cleanupUserStep() {
    return new StepBuilder("cleanupUserStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          userService.hardDeleteOldUsers();
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }

}
