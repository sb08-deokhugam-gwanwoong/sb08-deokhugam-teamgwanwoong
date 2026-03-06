package com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    String nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    boolean hasNext
) {
}
