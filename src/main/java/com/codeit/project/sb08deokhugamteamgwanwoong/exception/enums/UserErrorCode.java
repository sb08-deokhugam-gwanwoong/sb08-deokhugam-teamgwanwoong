package com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

  EMAIL_ALREADY_EXISTS(1001, "EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
  LOGIN_FAILED(1002, "LOGIN_FAILED", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
  USER_NOT_FOUND(1003, "NOT_FOUND", HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다."),
  NICKNAME_ALREADY_EXISTS(1004, "NICKNAME_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
  WRONG_PASSWORD(1005, "WRONG_PASSWORD", HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
  VERIFICATION_CODE_EXPIRED(1006, "VERIFICATION_CODE_EXPIRED", HttpStatus.BAD_REQUEST, "인증번호가 만료되었거나 요청 이력이 없습니다."),
  VERIFICATION_CODE_MISMATCH(1007, "VERIFICATION_CODE_MISMATCH", HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
  EMAIL_SEND_FAILED(1008, "EMAIL_SEND_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 혹은 외부 SMTP 연동 문제로 메일을 보낼 수 없는 상태입니다.");

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
