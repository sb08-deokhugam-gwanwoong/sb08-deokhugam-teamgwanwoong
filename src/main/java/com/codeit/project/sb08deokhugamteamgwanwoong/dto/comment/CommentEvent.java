package com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment;

import java.util.UUID;

public record CommentEvent(
    UUID reviewAuthorId,
    UUID reviewId,
    String commentAuthorNickname,
    String content,
    UUID commentId
) {
}
