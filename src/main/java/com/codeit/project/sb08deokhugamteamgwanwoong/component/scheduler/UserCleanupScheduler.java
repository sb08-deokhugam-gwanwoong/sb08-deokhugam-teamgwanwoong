package com.codeit.project.sb08deokhugamteamgwanwoong.component.scheduler;

import com.codeit.project.sb08deokhugamteamgwanwoong.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupScheduler {

  private final UserService userService;

  // 매일 00시 00분 실행
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void cleanupUser() {
    try {
      userService.hardDeleteOldUsers();
    } catch (Exception e) {
      log.error("[물리 삭제 스케쥴러 오류] 삭제 작업 중 예외 발생: {}", e.getMessage(), e);
    }
  }
}
