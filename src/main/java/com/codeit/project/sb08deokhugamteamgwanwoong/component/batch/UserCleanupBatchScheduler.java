package com.codeit.project.sb08deokhugamteamgwanwoong.component.batch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupBatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job cleanupUserJob;

  // 매일 00시 00분 실행
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void cleanupUser() {
    try {
      JobParameters params = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();
      jobLauncher.run(cleanupUserJob, params);

      String formattedTime = LocalDateTime.now()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

      log.info("[유저 삭제 배치 실행 성공] 실행 시간: {}, 파라미터: {}", formattedTime, params);

    } catch (Exception e) {
      log.error("[유저 삭제 배치 실행 오류] 발생 시간: {}", LocalDateTime.now(), e);
    }
  }
}
