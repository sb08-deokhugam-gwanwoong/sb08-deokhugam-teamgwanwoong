package com.codeit.project.sb08deokhugamteamgwanwoong.service.scheduler;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.component.scheduler.NotificationScheduler;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationSchedulerTest {

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private NotificationScheduler notificationScheduler;

  @Test
  @DisplayName("알림 삭제(스케쥴러) 성공: 예외 없이 정상적으로 삭제 서비스가 호출되어야 한다.")
  void cleanupNotifications_Test() {

    // When - 정상 실행
    notificationScheduler.cleanupNotifications();

    // Then
    verify(notificationService).deleteOldNotifications();
  }

  @Test
  @DisplayName("알림 삭제(스케쥴러) 실패: 서비스 호출 중 예외 발생 시 catch 블록에서 처리되어야 한다.")
  void cleanupNotifications_Fail_Test() {
    // Given
    doThrow(new RuntimeException("Error"))
        .when(notificationService).deleteOldNotifications();

    // When - 내부 try-catch 때문에 예외가 밖으로 터지지 않는다.
    notificationScheduler.cleanupNotifications();

    // Then - 예외 발생했어도 서비스 호출 시도 있었는지 검증
    verify(notificationService).deleteOldNotifications();
  }
}
