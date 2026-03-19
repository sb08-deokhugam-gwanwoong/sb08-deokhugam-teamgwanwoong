package com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification;

import java.util.UUID;

public record NotificationEvent(
    UUID toUserId,
    UUID reviewId,
    String message
) {
}
