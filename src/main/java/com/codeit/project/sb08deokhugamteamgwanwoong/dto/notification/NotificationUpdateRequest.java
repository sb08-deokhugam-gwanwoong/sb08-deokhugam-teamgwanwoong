package com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification;

import jakarta.validation.constraints.NotNull;

public record NotificationUpdateRequest(
    @NotNull(message = "확인 여부는 필수입니다.")
    Boolean confirmed
) {
}
