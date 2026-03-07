package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CursorPageResponseCommentDto;
import java.time.Instant;
import java.util.UUID;

public interface CommentService {
  CommentDto create(CommentCreateRequest request);

  CursorPageResponseCommentDto findAllComments(UUID reviewId, Instant cursorCreatedAt, int size);

  CommentDto findById(UUID commentId);

  CommentDto update(UUID commentId, UUID userId, CommentUpdateRequest request);

  //논리삭제
  void softDelete(UUID commentId, UUID userId);

  //물리삭제
  void hardDelete(UUID commentId, UUID userId);
}
