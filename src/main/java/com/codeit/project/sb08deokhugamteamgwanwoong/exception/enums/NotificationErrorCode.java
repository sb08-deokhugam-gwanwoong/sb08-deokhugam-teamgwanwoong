package com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

  NOTIFICATION_NOT_FOUND(3001, "NOT_FOUND", HttpStatus.NOT_FOUND, "해당 알림이 존재하지 않습니다."),
  NOTIFICATION_FORBIDDEN(3002, "FORBIDDEN", HttpStatus.FORBIDDEN, "해당 알림을 수정할 권한이 없습니다.");

  private final int numeric;
  private final String errorKey;
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getDomain() {
    return "NOTIFICATION";
  }

  @Override
  public String getCode() {
    return getDomain() + "-" + getErrorKey();
  }
}
