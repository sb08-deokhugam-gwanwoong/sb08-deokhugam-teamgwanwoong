package com.codeit.project.sb08deokhugamteamgwanwoong.dto.user;

import java.time.OffsetDateTime;

public record UserDto(
    Long id,
    String email,
    String nickname,
    OffsetDateTime createdAt
) {
}
