package com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePowerUserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.DashboardPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserPasswordUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
public interface UserApi {

  @Operation(
      summary = "회원가입",
      description = "새로운 사용자를 등록합니다.",
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "회원가입 성공",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (입력값 검증 실패)",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "409",
              description = "이메일 중복",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          )
      }
  )
  @PostMapping
  ResponseEntity<UserDto> createUser(@RequestBody @Valid UserRegisterRequest request);

  @Operation(
      summary = "로그인",
      description = "사용자 로그인을 처리합니다.",
      operationId = "login",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "로그인 성공",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "401",
              description = "로그인 실패 (이메일 또는 비밀번호 불일치)",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (입력값 검증 실패)",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          )
      }
  )
  @PostMapping("/login")
  ResponseEntity<UserDto> login(@RequestBody @Valid UserLoginRequest request);

  @Operation(
      summary = "사용자 정보 조회",
      description = "사용자 ID로 상세 정보를 조회합니다.",
      operationId = "getUser",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "사용자 정보 조회 성공",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "사용자 정보 없음",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          )
      }
  )
  @GetMapping("/{userId}")
  ResponseEntity<UserDto> getUser(@PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId);

  @Operation(
      summary = "사용자 정보 수정",
      description = "사용자의 닉네임을 수정합니다.",
      operationId = "updateUser",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "사용자 정보 수정 성공",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (입력값 검증 실패)",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "403",
              description = "사용자 정보 수정 권한 없음",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "사용자 정보 없음",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = UserDto.class))
          )
      }
  )
  @PatchMapping("/{userId}")
  ResponseEntity<UserDto> updateUser(@PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId, @RequestBody @Valid UserUpdateRequest request);

  @Operation(
      summary = "사용자 비밀번호 수정",
      description = "사용자의 비밀번호를 수정합니다.",
      operationId = "updateUserPassword",
      responses = {
          @ApiResponse(
              responseCode = "204",
              description = "사용자 비밀번호 수정 성공"
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (입력값 검증 실패)"
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
  @PatchMapping("/{userId}/password")
  ResponseEntity<Void> updatePassword(@PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId, @RequestBody @Valid UserPasswordUpdateRequest request);

  @Operation(
      summary = "사용자 논리 삭제",
      description = "사용자를 논리적으로 삭제합니다.",
      operationId = "deleteUser",
      responses = {
          @ApiResponse(
              responseCode = "204",
              description = "사용자 삭제 성공"
          ),
          @ApiResponse(
              responseCode = "403",
              description = "사용자 삭제 권한 없음"
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
  @DeleteMapping("/{userId}")
  ResponseEntity<Void> deleteUser(@PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId);

  @Operation(
      summary = "사용자 물리 삭제",
      description = "사용자를 물리적으로 삭제합니다.",
      operationId = "permanentDeleteUser",
      responses = {
          @ApiResponse(
              responseCode = "204",
              description = "사용자 삭제 성공"
          ),
          @ApiResponse(
              responseCode = "403",
              description = "사용자 삭제 권한 없음"
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
  @DeleteMapping("/{userId}/hard")
  ResponseEntity<Void> hardDeleteUser(@PathVariable @Parameter(description = "사용자 ID", required = true) UUID userId);

  @Operation(
      summary = "파워 유저 목록 조회",
      description = "기간별 파워 유저 목록을 조회합니다.",
      operationId = "getPowerUsers",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "파워 유저 목록 조회 성공",
              content = @Content(schema = @Schema(implementation = CursorPageResponsePowerUserDto.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)",
              content = @Content(schema = @Schema(implementation = CursorPageResponsePowerUserDto.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "서버 내부 오류",
              content = @Content(schema = @Schema(implementation = CursorPageResponsePowerUserDto.class))
          )
      }
  )
  @GetMapping("/power")
  ResponseEntity<CursorPageResponsePowerUserDto> getPowerUsers(@ParameterObject @ModelAttribute DashboardPageRequest request);
}
