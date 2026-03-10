package com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard;

import java.time.Instant;
import java.util.List;

public record CursorPageResponsePopularBookDto(
		List<PopularBookDto> content,
		String nextCursor,
		Instant nextAfter,
		Integer size,
		Long totalElements,
		Boolean hasNext
) {
}
