package com.example.coupangclone.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequestDto {

    @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "유효한 이메일 주소를 입력해주세요.")
    @NotNull(message = "E-mail을 입력해주세요.")
    private String email;

    @Size(min = 8, max = 15, message = "비밀번호는 최소 8자에서 15자 사이로만 가능합니다.")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])(?=.*\\p{Punct}).+$", message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야됩니다.")
    @NotNull(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotNull(message = "성함을 입력해주세요.")
    private String name;

    @NotNull(message = "전화번호를 입력해주세요.")
    private String tel;

    private String gender;

}
