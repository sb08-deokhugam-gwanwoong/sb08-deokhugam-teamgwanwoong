package com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {
  COMMENT_NOT_FOUND(2001, "NOT_FOUND", HttpStatus.NOT_FOUND, "해당 댓글이 존재하지 않습니다."),
  REVIEW_NOT_FOUND(2002, "REVIEW_NOT_FOUND", HttpStatus.NOT_FOUND, "댓글을 달려는 리뷰를 찾을 수 없습니다."),
  UNAUTHORIZED_COMMENT_ACCESS(2003, "UNAUTHORIZED", HttpStatus.FORBIDDEN, "사용자 본인의 댓글만 수정/삭제할 수 있습니다.");

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