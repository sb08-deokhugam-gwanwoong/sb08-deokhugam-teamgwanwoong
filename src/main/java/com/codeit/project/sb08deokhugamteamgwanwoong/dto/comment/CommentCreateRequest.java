package com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment;

import java.util.UUID;

public record CommentCreateRequest(
  UUID reviewId,
  UUID userId,
  String content
){
}
