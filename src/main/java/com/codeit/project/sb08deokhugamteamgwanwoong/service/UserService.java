package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import java.util.UUID;

public interface UserService {
  UserDto create(UserRegisterRequest request);

  UserDto login(UserLoginRequest request);

  UserDto getUserById(UUID userId);
}
