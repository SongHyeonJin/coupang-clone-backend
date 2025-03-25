package com.example.coupangclone.global.dto;

import com.example.coupangclone.global.exception.ExceptionEnum;
import lombok.Getter;

@Getter
public class ErrorResponseDto {

    private int status;
    private String type;
    private String msg;

    public ErrorResponseDto(int status, String type, String msg) {
        this.status = status;
        this.type = type;
        this.msg = msg;
    }

    public ErrorResponseDto(ExceptionEnum exceptionEnum) {
        this.status = exceptionEnum.getStatus();
        this.type = exceptionEnum.getType();
        this.msg = exceptionEnum.getMsg();
    }

}
