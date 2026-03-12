package com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BookErrorCode implements ErrorCode {

  DUPLICATE_ISBN(5001, "DUPLICATE_ISBN", HttpStatus.CONFLICT, "이미 존재하는 ISBN입니다."),
  BOOK_NOT_FOUND(5002, "NOT_FOUND", HttpStatus.NOT_FOUND, "해당 도서를 찾을 수 없습니다."),
  OCR_TEXT_NOT_FOUND(5003, "NOT_FOUND", HttpStatus.NOT_FOUND, "OCR 인식 실패. 텍스트를 찾을 수 없습니다."),
  OCR_BARCODE_NOT_FOUND(5004, "NOT_FOUND", HttpStatus.NOT_FOUND, "이미지에서 바코드를 찾을 수 없습니다. 다시 인식해주세요."),
  OCR_SERVER_ERROR(5005, "EXTERNAL_API_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "외부 OCR 서버와 통신할 수 없습니다. 잠시 후 다시 시도해주세요.");

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
