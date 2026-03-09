package com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public record CursorPageResponseCommentDto(
    List<CommentDto> content,      // 페이지 내용 (배열)
    String nextCursor,               // 다음 페이지 커서 (마지막 요소의 ID)
    Instant nextAfter,             // 마지막 요소의 생성 시간
    Integer size,                  // 페이지 크기
    Long totalElements,            // 총 요소 수
    Boolean hasNext
) {
}
