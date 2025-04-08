package com.example.coupangclone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "add")
public class BasicResponseDto {

    private int statusCode;
    private String msg;

    public static BasicResponseDto addSuccess(String msg) {
        return add(HttpStatus.OK.value(), msg);
    }

    public static BasicResponseDto addBadRequest(String msg) {
        return add(HttpStatus.BAD_REQUEST.value(), msg);
    }

}
