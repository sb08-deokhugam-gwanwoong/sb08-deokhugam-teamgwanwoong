package com.codeit.project.sb08deokhugamteamgwanwoong.component.scheduler;

import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

  private final NotificationService notificationService;

  // 매일 01시 00분 실행
  @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
  public void cleanupNotifications() {
    try {
      notificationService.deleteOldNotifications();
    } catch (Exception e) {
      log.error("[알림 삭제 스케쥴러 오류] 삭제 작업 중 예외 발생: {}", e.getMessage(), e);
    }
  }
}
