package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

import java.util.UUID;

public record ReviewPageRequest(
        UUID userId,
        UUID bookId,
        // 작성자 닉네임 | 내용
        String keyword,
        // 정렬 기준(createdAt | rating)
        String orderBy,
        // 정렬 방향 (ASC, DESC)
        String direction,
        // 커서 페이지네이션 커서
        String cursor,
        // 보조 커스(createdAt)
        String after,
        // 페이지 크기
        Integer limit,
        // 요청자 ID
        UUID requestUserId
) {
    public ReviewPageRequest {
        if (limit == null || limit <= 0) {
            limit = 50;
        }
    }
}
