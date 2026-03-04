package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.support.ControllerTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

@WebMvcTest(controllers = { UserController.class })
public class UserControllerTest extends ControllerTestSupport {

  @Test
  @DisplayName("회원가입: 올바른 입력값이 들어오면 201 응답을 반환해야 한다.")
  void createUserTest() throws Exception {
    // Given
    UserRegisterRequest request = new UserRegisterRequest("test@test.com", "Tester", "password123!");
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
}
