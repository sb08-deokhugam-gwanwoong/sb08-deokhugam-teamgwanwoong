package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs.CommentApi;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CursorPageResponseCommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.CommentService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentApi {

  private final CommentService commentService;

  //댓글 등록
  //새로운 댓글을 등록
  @Override
  public ResponseEntity<CommentDto> create(@Valid @RequestBody CommentCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(commentService.create(request));
  }

  //리뷰 댓글 목록 조회
  //특정 리뷰에 달린 댓글 목록을 시간순으로 조회
  @Override
  public ResponseEntity<CursorPageResponseCommentDto> findAllComments(
      @RequestParam UUID reviewId,
      @RequestParam(required = false, defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, name = "after") Instant cursorCreatedAt,
      @RequestParam(defaultValue = "50", name = "limit") int size) {
    return ResponseEntity.ok(commentService.findAllComments(reviewId, cursorCreatedAt, size));
  }

  //댓글 상세 정보 조회
  //특정 댓글의 상세 정보를 조회
  @Override
  public ResponseEntity<CommentDto> findById(@PathVariable UUID commentId) {
    return ResponseEntity.ok(commentService.findById(commentId));
  }

  //댓글 수정
  //본인이 작성한 댓글을 수정
  @Override
  public ResponseEntity<CommentDto> update(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @Valid @RequestBody CommentUpdateRequest request) {
    return ResponseEntity.ok(commentService.update(commentId, userId, request));
  }

  //댓글 논리 삭제
  //본인이 작성한 댓글을 논리적으로 삭제
  @Override
  public ResponseEntity<Void> delete(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
    commentService.softDelete(commentId, userId);
    return ResponseEntity.noContent().build();
  }

  //댓글 물리 삭제
  //본인이 작성한 댓글을 물리적으로 삭제
  @Override
  public ResponseEntity<Void> hardDelete(
      @PathVariable UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
    commentService.hardDelete(commentId, userId);
    return ResponseEntity.noContent().build();
  }
}
