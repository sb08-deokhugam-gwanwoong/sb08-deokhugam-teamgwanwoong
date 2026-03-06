package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs.NotificationApi;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

  private final NotificationService notificationService;

  @Override
  public ResponseEntity<NotificationDto> updateNotification(
      @PathVariable UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @RequestBody @Valid NotificationUpdateRequest request) {

    NotificationDto response = notificationService.update(notificationId, requestUserId, request);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> markAllAsRead(UUID requestUserId) {

    notificationService.allConfirmNotification(requestUserId);

    return ResponseEntity.noContent().build();
  }
}
