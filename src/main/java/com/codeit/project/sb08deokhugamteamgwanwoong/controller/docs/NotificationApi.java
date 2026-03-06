package com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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
}
