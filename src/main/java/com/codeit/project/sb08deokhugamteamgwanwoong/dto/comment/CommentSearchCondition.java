package com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommentSearchCondition(
    UUID reviewId,
    String cursor,
    Instant after,
    Integer limit
    ) {

}
