package com.example.coupangclone.exception;

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
    NOT_ALLOW(HttpStatus.FORBIDDEN.value(), "권한이 없습니다.", "USER"),
    NEED_LOGIN(HttpStatus.UNAUTHORIZED.value(), "인증이 필요합니다.", "USER"),
    PARENT_CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "상위 카테고리가 존재하지 않습니다.", "ITEM"),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "카테고리를 찾을 수 없습니다..", "ITEM"),
    BRAND_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "브랜드를 찾을 수 없습니다..", "ITEM"),
    CATEGORY_DUPLICATION(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 카테고리입니다.", "ITEM"),
    BRAND_DUPLICATION(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 브랜드입니다.", "ITEM"),
    IMAGE_REQUIRED(HttpStatus.BAD_REQUEST.value(), "이미지를 첨부해주세요.", "ITEM"),
    IMAGE_FILENAME_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "파일 이름이 존재하지 않습니다.", "ITEM"),
    IMAGE_EXTENSION_MISSING(HttpStatus.BAD_REQUEST.value(), "확장자가 없는 파일은 업로드 할 수 없습니다.", "ITEM");

    private final int status;
    private final String msg;
    private final String type;

    ExceptionEnum(int status, String msg, String type) {
        this.status = status;
        this.msg = msg;
        this.type = type;
    }
}
