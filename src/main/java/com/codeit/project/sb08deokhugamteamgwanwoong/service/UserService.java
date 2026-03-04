package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;

public interface UserService {
  UserDto create(UserRegisterRequest request);
}
