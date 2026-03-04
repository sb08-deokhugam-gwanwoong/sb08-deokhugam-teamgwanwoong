package com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
}
