package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import java.util.UUID;

public interface UserService {
  UserDto create(UserRegisterRequest request);

  UserDto login(UserLoginRequest request);

  UserDto getUserById(UUID userId);

  UserDto update(UUID userId, UserUpdateRequest request);

  void delete(UUID userId);

  void hardDelete(UUID userId);
}
