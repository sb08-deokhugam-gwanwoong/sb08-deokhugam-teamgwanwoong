package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import java.time.Instant;
import lombok.Builder;
import org.springframework.data.domain.Sort;

// 서비스 계층에서 Repository로 넘겨줄 DTO
@Builder
public record BookSearchCondition(
    String keyword,
    String cursor,
    Instant after,
    String orderBy,
    Sort.Direction direction
) {

}
