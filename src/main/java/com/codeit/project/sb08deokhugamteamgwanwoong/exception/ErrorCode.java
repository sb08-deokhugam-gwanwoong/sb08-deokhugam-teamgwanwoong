package com.codeit.project.sb08deokhugamteamgwanwoong.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    int getNumeric();               // 1001
    String getDomain();             // EMPL
    String getErrorKey();           // NOT_FOUND
    String getCode();               // EMPL-NOT_FOUND
    HttpStatus getHttpStatus();     // 404
    String getMessage();            // 직원을 찾을 수 없습니다.
}
