package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserLoginRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserPasswordUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserResetPasswordRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserUpdateRequest;
import java.util.UUID;

public interface UserService {
  UserDto create(UserRegisterRequest request);

  UserDto login(UserLoginRequest request);

  UserDto getUserById(UUID userId);

  UserDto update(UUID userId, UserUpdateRequest request);

  void updatePassword(UUID userId, UserPasswordUpdateRequest request);

  void sendVerificationCode(String email);

  void verifyCode(String email, String inputCode);

  void resetPassword(UserResetPasswordRequest request);

  void delete(UUID userId);

  void hardDelete(UUID userId);

  // 논리 삭제 후 일정 기간 지난 사용자 물리 삭제 (스케쥴러 전용)
  void hardDeleteOldUsers();
}
