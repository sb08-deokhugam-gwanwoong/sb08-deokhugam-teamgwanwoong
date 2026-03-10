package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.support.ControllerTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.CursorPageResponseNotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationUpdateRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;

@WebMvcTest(controllers = {NotificationController.class})
public class NotificationControllerTest extends ControllerTestSupport {

  @Nested
  @DisplayName("알림 상태 업데이트 API 검증")
  class UpdateOperations {

    @Test
    @DisplayName("알림 읽음 상태 업데이트 성공: 특정 알림의 읽음 상태를 반환하고, DTO를 반환한다.")
    void updateNotificationTest() throws Exception {
      // Given
      UUID notificationId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      NotificationUpdateRequest request = new NotificationUpdateRequest(true);

      NotificationDto response = new NotificationDto(
          notificationId,
          userId,
          UUID.randomUUID(),
          "리뷰 내용",
          "[Tester]님이 나의 리뷰를 좋아합니다.",
          true,
          Instant.now(),
          Instant.now()
      );

      given(notificationService.update(eq(notificationId), eq(userId), any(NotificationUpdateRequest.class)))
          .willReturn(response);

      // When & Then
      mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
              .header("Deokhugam-Request-User-ID", userId) // 필수 헤더 포함
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(notificationId.toString()))
          .andExpect(jsonPath("$.confirmed").value(true))
          .andExpect(jsonPath("$.reviewContent").value("리뷰 내용"));
    }

    @Test
    @DisplayName("모든 알림 읽음 상태 업데이트 성공: 유저의 모든 알림을 읽음 처리하고, 204 응답을 반환해야 한다.")
    void markAllAsRead_Test() throws Exception {
      // Given
      UUID userId = UUID.randomUUID();

      // When & Then
      mockMvc.perform(patch("/api/notifications/read-all")
              .header("Deokhugam-Request-User-ID", userId))
          .andExpect(status().isNoContent());

      verify(notificationService).allConfirmNotification(eq(userId));
    }
  }

  @Nested
  @DisplayName("알림 목록 조회 API 검증")
  class GetOperations {

    @Test
    @DisplayName("알림 목록 조회 성공: 커서 페이지네이션을 통해 알림 목록 조회를 수행한다.")
    void getNotifications_Test() throws Exception {
      // Given
      UUID userId = UUID.randomUUID();

      CursorPageResponseNotificationDto response = CursorPageResponseNotificationDto.of(
          Collections.emptyList(), 0, 20, false);

      given(notificationService.getNotifications(eq(userId), eq(Direction.DESC), any(), any(), eq(20)))
          .willReturn(response);

      // When & Then
      mockMvc.perform(get("/api/notifications")
              .param("userId", userId.toString())
              .param("direction", "DESC")
              .param("limit", "20"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(0))
          .andExpect(jsonPath("$.hasNext").value(false));
    }
  }
}
