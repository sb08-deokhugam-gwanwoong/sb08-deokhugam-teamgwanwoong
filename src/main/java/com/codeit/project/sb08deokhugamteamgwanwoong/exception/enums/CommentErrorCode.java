package com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {
  COMMENT_NOT_FOUND(2001, "NOT_FOUND", HttpStatus.NOT_FOUND, "해당 댓글이 존재하지 않습니다."),
  COMMENT_NOT_OWNER(2002, "FORBIDDEN", HttpStatus.FORBIDDEN, "해당 댓글에 대한 권한이 없습니다."),
  COMMENT_UPDATE_DENIED(2003, "FORBIDDEN", HttpStatus.FORBIDDEN, "사용자 본인의 댓글만 수정할 수 있습니다."),
  COMMENT_DELETE_DENIED(2004, "FORBIDDEN", HttpStatus.FORBIDDEN, "본인의 댓글만 삭제할 수 있습니다.");

  private final int numeric;
  private final String errorKey;
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getDomain() {
    return "COMMENT";
  }

  @Override
  public String getCode() {
    return getDomain() + "-" + getErrorKey();
  }
}