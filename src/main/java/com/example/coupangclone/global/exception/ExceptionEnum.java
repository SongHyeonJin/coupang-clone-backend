package com.example.coupangclone.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionEnum {
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST.value(), "중복된 이메일입니다.");

    private final int status;
    private final String msg;

    ExceptionEnum(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
}
