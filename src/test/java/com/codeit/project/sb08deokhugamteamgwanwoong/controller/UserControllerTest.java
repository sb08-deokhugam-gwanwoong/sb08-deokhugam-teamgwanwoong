package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.support.ControllerTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserPasswordUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserResetPasswordRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserVerificationRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserVerifyCodeRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

@WebMvcTest(controllers = {UserController.class})
public class UserControllerTest extends ControllerTestSupport {

  @Test
  @DisplayName("회원가입: 올바른 입력값이 들어오면 201 응답을 반환해야 한다.")
  void createUserTest() throws Exception {
    // Given
    UserRegisterRequest request = new UserRegisterRequest("test@test.com", "Tester",
        "password123!");
    UserDto response = new UserDto(UUID.randomUUID(), "test@test.com", "Tester", Instant.now());

    given(userService.create(any(UserRegisterRequest.class))).willReturn(response);

    // When & Then
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.nickname").value("Tester"));
  }

  @Test
  @DisplayName("로그인: 가입된 정보로 로그인을 시도하면 성공하고, 200 응답을 반환해야 한다.")
  void loginUserTest() throws Exception {
    // Given
    String email = "test@test.com";
    String password = "password123!";
    String nickname = "Tester";

    UserLoginRequest request = new UserLoginRequest(email, password);
    UserDto response = new UserDto(UUID.randomUUID(), email, nickname, Instant.now());

    given(userService.login(any(UserLoginRequest.class))).willReturn(response);

    // When & Then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.nickname").value(nickname))
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  @DisplayName("유저 정보 조회: 존재하는 유저 ID로 요청하면 200 응답과 유저의 정보를 반환한다.")
  void getUserTest() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    UserDto response = new UserDto(userId, "test@test.com", "Tester", Instant.now());

    given(userService.getUserById(userId)).willReturn(response);

    // When & Then
    mockMvc.perform(get("/api/users/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("test@test.com"));
  }

  @Test
  @DisplayName("유저 정보 조회 실패: 존재하지 않는 유저 ID로 요청하면 404 Not Found를 반환한다.")
  void getUser_NotFound_Fail_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();

    given(userService.getUserById(userId))
        .willThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND));

    // When & Then
    mockMvc.perform(get("/api/users/{userId}", userId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("유저 정보 수정: 새로운 닉네임으로 수정을 요청하면 200 응답과 수정된 유저의 정보를 반환한다.")
  void updateUser_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("NewTester");
    UserDto response = new UserDto(userId, "test@test.com", "NewTester", Instant.now());

    given(userService.update(any(UUID.class), any(UserUpdateRequest.class))).willReturn(response);

    // When & Then
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.nickname").value("NewTester"));
  }

  @Test
  @DisplayName("유저 정보 수정 실패: 이미 존재하는 닉네임으로 수정을 시도하면 에러 응답을 반환한다.")
  void updateUser_Fail_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("DuplicateTester");

    given(userService.update(any(UUID.class), any(UserUpdateRequest.class)))
        .willThrow(new BusinessException(UserErrorCode.NICKNAME_ALREADY_EXISTS));

    mockMvc.perform(patch("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("유저 논리 삭제: 존재하는 유저 ID로 요청하면 204 No Content를 반환한다.")
  void deleteUser_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("유저 물리 삭제: 존재하는 유저 ID로 요청하면 204 No Content를 반환한다.")
  void hardDeleteUser_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}/hard", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("유저 논리 삭제 실패: 존재하지 않는 유저 ID로 요청하면 404 Not Found를 반환한다.")
  void deleteUser_NotFound_Fail_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();

    willThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND))
        .given(userService).delete(userId);

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("유저 물리 삭제 실패: 존재하지 않는 유저 ID로 요청하면 404 Not Found를 반환한다.")
  void hardDeleteUser_NotFound_Fail_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();

    willThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND))
        .given(userService).hardDelete(userId);

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}/hard", userId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("비밀번호 수정: 현재 비밀번호가 일치하면 204 No Content를 반환한다.")
  void updatePassword_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password1234!", "newPass1234!");

    // When & Then
    mockMvc.perform(patch("/api/users/{userId}/password", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("비밀번호 수정 실패: 현재 비밀번호가 틀리면 400 Bad Request를 반환한다.")
  void updatePassword_Fail_Test() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password1234!", "newPass1234!");

    willThrow(new BusinessException(UserErrorCode.WRONG_PASSWORD))
        .given(userService).updatePassword(any(UUID.class), any(UserPasswordUpdateRequest.class));

    // When & Then
    mockMvc.perform(patch("/api/users/{userId}/password", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("비밀번호 최종 재설정: 인증 완료 후 비밀번호를 변경하면 204 No Content를 반환한다.")
  void resetPassword_Test() throws Exception {
    // Given
    UserResetPasswordRequest request = new UserResetPasswordRequest("test@test.com", "newPass1234!");

    // When & Then
    mockMvc.perform(patch("/api/users/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("인증번호 발송: 존재하는 이메일로 요청하면 200 OK를 반환한다.")
  void sendVerificationCode_Test() throws Exception {
    // Given
    UserVerificationRequest request = new UserVerificationRequest("test@test.com");

    // When & Then
    mockMvc.perform(post("/api/users/password/verification-code")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("인증번호 검증: 올바른 번호를 입력하면 200 OK를 반환한다.")
  void verifyCode_Test() throws Exception {
    // Given
    UserVerifyCodeRequest request = new UserVerifyCodeRequest("test@test.com", "123456");

    // When & Then
    mockMvc.perform(post("/api/users/password/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("인증번호 검증 실패: 번호가 틀리거나 만료되면 400 Bad Request를 반환한다.")
  void verifyCode_Fail_Test() throws Exception {
    // Given
    UserVerifyCodeRequest request = new UserVerifyCodeRequest("test@test.com", "999999");

    willThrow(new BusinessException(UserErrorCode.VERIFICATION_CODE_MISMATCH))
        .given(userService).verifyCode(anyString(), anyString());

    // When & Then
    mockMvc.perform(post("/api/users/password/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
