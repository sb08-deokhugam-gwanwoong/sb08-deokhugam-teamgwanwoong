package com.codeit.project.sb08deokhugamteamgwanwoong.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {
    REVIEW_NOT_FOUND(4001, "NOT_FOUND", HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(4002, "ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 작성된 리뷰가 존재합니다."),
    REVIEW_AUTHOR_MISMATCH(4003, "AUTHOR_MISMATCH", HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 수정할 수 있습니다.");

    private final int numeric;
    private final String errorKey;
    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getDomain() {
        return "REVIEW";
    }

    @Override
    public String getCode() {
        return getDomain() + "-" + getErrorKey();
    }
}