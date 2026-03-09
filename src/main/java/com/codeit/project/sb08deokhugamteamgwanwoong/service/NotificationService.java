package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.CursorPageResponseNotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort.Direction;

public interface NotificationService {
  void createNotification(User user, Review review, String message);

  NotificationDto update(UUID notificationId, UUID requestUserId, NotificationUpdateRequest request);

  void allConfirmNotification(UUID requestUserId);

  CursorPageResponseNotificationDto getNotifications(
      UUID userId,
      Direction direction,
      Instant cursor,
      Instant after,
      int limit
  );
}
