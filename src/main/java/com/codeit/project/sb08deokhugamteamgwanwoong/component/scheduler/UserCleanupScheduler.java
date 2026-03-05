package com.codeit.project.sb08deokhugamteamgwanwoong.component.scheduler;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupScheduler {

  private final UserRepository userRepository;

  // 매일 00시 00분 실행
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  @Transactional
  public void cleanupUser() {

    try {
      Instant oneDayAgo = Instant.now().minus(Duration.ofDays(1));

      List<User> deleteTargets = userRepository.findAllExpiredUsers(oneDayAgo);

      if (!deleteTargets.isEmpty()) {

        log.info("[물리 삭제 스케쥴러 시작] 삭제 대상 유저 수: {}", deleteTargets.size());

        userRepository.deleteAll(deleteTargets);

        log.info("[물리 삭제 스케쥴러 완료] 논리 삭제 후 1일이 지난 유저 삭제 완료");
      }

    } catch (Exception e) {
      log.error("[물리 삭제 스케쥴러 오류] 삭제 작업 중 예외 발생: {}", e.getMessage(), e);
    }
  }
}
