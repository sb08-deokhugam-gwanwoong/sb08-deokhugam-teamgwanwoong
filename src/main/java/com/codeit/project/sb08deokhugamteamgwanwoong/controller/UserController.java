package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs.UserApi;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePowerUserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.DashboardPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.DashboardService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  private final DashboardService dashboardService;

  @Override
  public ResponseEntity<UserDto> createUser(@RequestBody @Valid UserRegisterRequest request) {

    UserDto response = userService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  public ResponseEntity<UserDto> login(@RequestBody @Valid UserLoginRequest request) {

    UserDto response = userService.login(request);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {

    UserDto response = userService.getUserById(userId);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId, @RequestBody @Valid UserUpdateRequest request) {

    UserDto response = userService.update(userId, request);

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {

    userService.delete(userId);

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> hardDeleteUser(@PathVariable UUID userId) {
    userService.hardDelete(userId);

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<CursorPageResponsePowerUserDto> getPowerUsers(@ModelAttribute DashboardPageRequest request) {

    CursorPageResponsePowerUserDto response = dashboardService.getPowerUsers(request);

    return ResponseEntity.ok(response);
  }
}
