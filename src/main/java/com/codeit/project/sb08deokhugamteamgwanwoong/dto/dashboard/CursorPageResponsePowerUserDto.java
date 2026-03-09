package com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard;

import java.time.Instant;
import java.util.List;

public record CursorPageResponsePowerUserDto(
		List<PowerUserDto> content,
		String nextCursor,
		Instant nextAfter,
		Integer size,
		Long totalElements,
		Boolean hasNext
) {
}
