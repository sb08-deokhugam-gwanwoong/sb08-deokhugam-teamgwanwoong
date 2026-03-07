package com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID userId,
    UUID reviewId,
    String reviewContent,
    String message,
    boolean confirmed,
    Instant createdAt,
    Instant updatedAt
) {
}
