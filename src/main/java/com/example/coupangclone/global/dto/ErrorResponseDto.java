package com.example.coupangclone.global.dto;

import com.example.coupangclone.global.exception.ExceptionEnum;
import lombok.Getter;

@Getter
public class ErrorResponseDto {

    private int status;
    private String msg;

    public ErrorResponseDto(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public ErrorResponseDto(ExceptionEnum exceptionEnum) {
        this.status = exceptionEnum.getStatus();
        this.msg = exceptionEnum.getMsg();
    }

}
