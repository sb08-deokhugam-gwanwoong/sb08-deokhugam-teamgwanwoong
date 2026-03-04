package com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

  EMAIL_ALREADY_EXISTS(1001, "EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");

  private final int numeric;
  private final String errorKey;
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getDomain() {
    return "USER";
  }

  @Override
  public String getCode() {
    return getDomain() + "-" + getErrorKey();
  }
}
