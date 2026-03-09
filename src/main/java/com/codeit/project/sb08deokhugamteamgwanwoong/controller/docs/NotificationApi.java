package com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.CursorPageResponseNotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "알림 관리", description = "알림 관련 API")
public interface NotificationApi {

  @Operation(
      summary = "알림 읽음 상태 업데이트",
      description = "특정 알림의 읽음 상태를 업데이트합니다.",
      operationId = "updateNotification",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "알림 상태 업데이트 성공",
              content = @Content(schema = @Schema(implementation = NotificationDto.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
              content = @Content(schema = @Schema(implementation = NotificationDto.class))
          ),
          @ApiResponse(
              responseCode = "403",
              description = "알림 수정 권한 없음",
              content = @Content(schema = @Schema(implementation = NotificationDto.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "알림 정보 없음",
              content = @Content(schema = @Schema(implementation = NotificationDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = NotificationDto.class))
          )
      }
  )
  @PatchMapping("/{notificationId}")
  ResponseEntity<NotificationDto> updateNotification(
      @PathVariable @Parameter(description = "알림 ID", required = true) UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @RequestBody @Valid NotificationUpdateRequest request);

  @Operation(
      summary = "모든 알림 읽음 처리",
      description = "사용자의 모든 알림을 읽음 상태로 처리합니다.",
      operationId = "markAllAsRead",
      responses = {
          @ApiResponse(
              responseCode = "204",
              description = "알림 읽음 처리 성공"
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (사용자 ID 누락)"
          ),
          @ApiResponse(
              responseCode = "404",
              description = "사용자 정보 없음"
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류"
          )
      }
  )
  @PatchMapping("/read-all")
  ResponseEntity<Void> markAllAsRead(@RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId);

  @Operation(
      summary = "알림 목록 조회",
      description = "사용자의 알림 목록을 조회합니다.",
      operationId = "getNotifications",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "알림 목록 조회 성공"
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 사용자 ID 누락)"
          ),
          @ApiResponse(
              responseCode = "404",
              description = "사용자 정보 없음"
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류"
          )
      }
  )
  @GetMapping
  ResponseEntity<CursorPageResponseNotificationDto> getNotifications(
      @RequestParam @Parameter(description = "사용자 ID", required = true) UUID userId,
      @RequestParam(required = false, defaultValue = "DESC") @Parameter(description = "정렬 방향") Direction direction,
      @RequestParam(required = false) @Parameter(description = "커서 페이지네이션 커서") Instant cursor,
      @RequestParam(required = false) @Parameter(description = "보조 커서(createdAt)") Instant after,
      @RequestParam(required = false, defaultValue = "20") @Parameter(description = "페이지 크기") int limit
      );
}
