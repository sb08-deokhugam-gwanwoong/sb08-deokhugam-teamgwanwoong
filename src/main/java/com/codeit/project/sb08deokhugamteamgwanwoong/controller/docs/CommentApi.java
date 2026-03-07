package com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
public interface CommentApi {

  @Operation(
      summary = "댓글 등록",
      description = "새로운 댓글을 등록합니다.",
      operationId = "createComment",
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "댓글 등록 성공",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (입력값 검증 실패)",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "리뷰 정보 없음",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          )
      }
  )
  @PostMapping
  ResponseEntity<CommentDto> create(@RequestBody @Valid CommentCreateRequest request);

  @Operation(
      summary = "리뷰 댓글 목록 조회",
      description = "특정 리뷰에 달린 댓글 목록을 시간순으로 조회합니다.",
      operationId = "getCommentsByReviewId",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "댓글 목록 조회 성공",
              content = @Content(schema = @Schema(implementation = CursorPageResponseCommentDto.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 리뷰 ID 누락)",
              content = @Content(schema = @Schema(implementation = CursorPageResponseCommentDto.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "리뷰 정보 없음",
              content = @Content(schema = @Schema(implementation = CursorPageResponseCommentDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = CursorPageResponseCommentDto.class))
          )
      }
  )
  @GetMapping
  ResponseEntity<CursorPageResponseCommentDto> findAllComments(
      @RequestParam @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
      @RequestParam(required = false, defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, name = "after") Instant cursorCreatedAt,
      @RequestParam(defaultValue = "50", name = "limit") int size);

  @Operation(
      summary = "댓글 상세 정보 조회",
      description = "특정 댓글의 상세 정보를 조회합니다.",
      operationId = "getComment",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "댓글 조회 성공",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "댓글 정보 없음",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          )
      }
  )
  @GetMapping("/{commentId}")
  ResponseEntity<CommentDto> findById(
      @PathVariable @Parameter(description = "댓글 ID", required = true) UUID commentId);

  @Operation(
      summary = "댓글 수정",
      description = "본인이 작성한 댓글을 수정합니다.",
      operationId = "updateComment",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "댓글 수정 성공",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "403",
              description = "댓글 수정 권한 없음",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "댓글 정보 없음",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = CommentDto.class))
          )
      }
  )
  @PatchMapping("/{commentId}")
  ResponseEntity<CommentDto> update(
      @PathVariable @Parameter(description = "댓글 ID", required = true) UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") @Parameter(description = "요청자 ID", required = true) UUID userId,
      @RequestBody @Valid CommentUpdateRequest request);

  @Operation(
      summary = "댓글 논리 삭제",
      description = "본인이 작성한 댓글을 논리적으로 삭제합니다.",
      operationId = "deleteComment",
      responses = {
          @ApiResponse(
              responseCode = "204",
              description = "댓글 삭제 성공"),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (요청자 ID 누락)"),
          @ApiResponse(
              responseCode = "403",
              description = "댓글 삭제 권한 없음"),
          @ApiResponse(
              responseCode = "404",
              description = "댓글 정보 없음"),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류")
      }
  )
  @DeleteMapping("/{commentId}")
  ResponseEntity<Void> delete(
      @PathVariable @Parameter(description = "댓글 ID", required = true) UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") @Parameter(description = "요청자 ID", required = true) UUID userId);

  @Operation(
      summary = "댓글 물리 삭제",
      description = "본인이 작성한 댓글을 물리적으로 삭제합니다.",
      operationId = "permanentDeleteComment",
      responses = {
          @ApiResponse(
              responseCode = "204",
              description = "댓글 삭제 성공"),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (요청자 ID 누락)"),
          @ApiResponse(
              responseCode = "403",
              description = "댓글 삭제 권한 없음"),
          @ApiResponse(
              responseCode = "404",
              description = "댓글 정보 없음"),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류")
      }
  )
  @DeleteMapping("/{commentId}/hard")
  ResponseEntity<Void> hardDelete(
      @PathVariable @Parameter(description = "댓글 ID", required = true) UUID commentId,
      @RequestHeader("Deokhugam-Request-User-ID") @Parameter(description = "요청자 ID", required = true) UUID userId);
}