package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserRegisterRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.UserMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserDto create(UserRegisterRequest request) {

    log.info("[회원가입 시작] email: {}, nickname: {}", request.email(), request.nickname());

    // 중복 이메일 확인
    if (userRepository.existsByEmail(request.email())) {
      log.warn("[회원가입 실패] 이미 존재하는 이메일입니다. email: {}", request.email());
      throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
    }

    User user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(request.password())
        .build();

    User savedUser = userRepository.save(user);

    log.info("[회원가입 완료] 새로운 유저 생성 성공. id: {}", savedUser.getId());

    return userMapper.toDto(savedUser);
  }
}
