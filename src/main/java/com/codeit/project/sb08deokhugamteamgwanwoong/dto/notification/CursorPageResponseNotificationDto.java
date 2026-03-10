package com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    Instant nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    boolean hasNext
) {

  // 정적 메서드 추출
  // 응답 형식에 관한 계산을 하는 부분이기에
  // 책임 분리를 위하여 메서드 추출
  public static CursorPageResponseNotificationDto of(
      List<NotificationDto> content,
      long totalElements,
      int limit,
      boolean hasNext
  ) {

    Instant nextCursor = null;
    Instant nextAfter = null;

    if (hasNext && !content.isEmpty()) {
      NotificationDto lastNotification = content.get(content.size() - 1);
      nextCursor = lastNotification.createdAt();
      nextAfter = lastNotification.createdAt();
    }

    return new CursorPageResponseNotificationDto(
        content,
        nextCursor,
        nextAfter,
        limit,
        totalElements,
        hasNext
    );
  }
}
