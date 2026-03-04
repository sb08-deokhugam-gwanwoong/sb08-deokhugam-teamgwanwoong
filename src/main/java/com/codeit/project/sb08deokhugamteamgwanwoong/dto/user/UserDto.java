package com.codeit.project.sb08deokhugamteamgwanwoong.dto.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDto(
    UUID id,
    String email,
    String nickname,
    OffsetDateTime createdAt
) {
}
