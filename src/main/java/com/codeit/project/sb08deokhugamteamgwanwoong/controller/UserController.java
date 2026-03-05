package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs.UserApi;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  public ResponseEntity<UserDto> createUser(UserRegisterRequest request) {

    UserDto response = userService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  public ResponseEntity<UserDto> login(UserLoginRequest request) {

    UserDto response = userService.login(request);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {

    UserDto response = userService.getUserById(userId);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<UserDto> updateUser(UUID userId, UserUpdateRequest request) {

    UserDto response = userService.update(userId, request);

    return ResponseEntity.ok(response);
  }

}
