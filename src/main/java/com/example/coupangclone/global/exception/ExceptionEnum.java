package com.example.coupangclone.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionEnum {
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST.value(), "중복된 이메일입니다.", "USER"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "사용자를 찾을 수 없습니다.", "USER"),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST.value(), "비밀번호가 일치하지 않습니다.", "USER"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), "유효하지 않는 JWT 서명 입니다.", "JWT"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED.value(), "만료된 JWT 토큰 입니다.", "JWT"),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST.value(), "지원되지 않는 JWT 토큰 입니다.", "JWT"),
    WRONG_TOKEN(HttpStatus.BAD_REQUEST.value(), "잘못된 JWT 토근입니다.", "JWT"),
    NOT_ALLOW(HttpStatus.BAD_REQUEST.value(), "권한이 없습니다.", "USER"),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "상위 카테고리가 존재하지 않습니다.", "ITEM");

    private final int status;
    private final String msg;
    private final String type;

    ExceptionEnum(int status, String msg, String type) {
        this.status = status;
        this.msg = msg;
        this.type = type;
    }
}
