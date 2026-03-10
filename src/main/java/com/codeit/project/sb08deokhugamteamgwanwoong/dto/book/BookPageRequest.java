package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

// 컨트롤러에서 URL 파라미터 받는 DTO
public record BookPageRequest(
    String keyword,
    String orderBy,
    String direction,
    String cursor,
    String after,
    Integer limit
) {
  public BookPageRequest {
    // 기본값 세팅
    if (limit == null || limit <= 0) {
      limit = 50;
    }
    if (orderBy == null || orderBy.isBlank()) {
      orderBy = "title";
    }
    if (direction == null || direction.isBlank()) {
      direction = "DESC";
    }
  }
}
