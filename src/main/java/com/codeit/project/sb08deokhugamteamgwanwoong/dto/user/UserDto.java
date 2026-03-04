package com.codeit.project.sb08deokhugamteamgwanwoong.dto.user;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id,
    String email,
    String nickname,
    Instant createdAt
) {
}
