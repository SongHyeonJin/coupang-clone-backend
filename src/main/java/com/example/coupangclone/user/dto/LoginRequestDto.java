package com.example.coupangclone.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {
    @NotNull(message = "E-mail을 입력해주세요.")
    private String email;
    @NotNull(message = "비밀번호를 입력해주세요.")
    private String password;

}
