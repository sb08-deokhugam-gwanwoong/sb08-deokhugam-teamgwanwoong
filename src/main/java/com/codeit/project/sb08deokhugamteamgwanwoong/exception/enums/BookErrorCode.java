package com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BookErrorCode implements ErrorCode {

  DUPLICATE_ISBN(5001, "DUPLICATE_ISBN", HttpStatus.CONFLICT, "이미 존재하는 ISBN입니다."),
  BOOK_NOT_FOUND(5002, "NOT_FOUND", HttpStatus.NOT_FOUND, "해당 도서를 찾을 수 없습니다.");

  private final int numeric;
  private final String errorKey;
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getDomain() {
    return "BOOK";
  }

  @Override
  public String getCode() {
    return getDomain() + "-" + getErrorKey();
  }
}
