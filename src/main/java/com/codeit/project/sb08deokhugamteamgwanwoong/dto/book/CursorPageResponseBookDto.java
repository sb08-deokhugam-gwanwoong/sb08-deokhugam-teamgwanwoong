package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record CursorPageResponseBookDto(
    List<BookDto> content,
    String nextCursor,
    Instant nextAfter,
    Integer size,
    Long totalElements,
    Boolean hasNext
) {
}
