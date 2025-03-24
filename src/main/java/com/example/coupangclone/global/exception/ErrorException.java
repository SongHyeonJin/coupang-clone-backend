package com.example.coupangclone.global.exception;

import lombok.Getter;

@Getter
public class ErrorException extends RuntimeException {

    private final ExceptionEnum exceptionEnum;

    public ErrorException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMsg());
        this.exceptionEnum = exceptionEnum;
    }
}
