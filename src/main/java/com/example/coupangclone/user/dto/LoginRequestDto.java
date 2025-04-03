package com.example.coupangclone.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequestDto {
    @Schema(description = "사용자 이메일(관리자이니 회원으로 하려면 회원 가입했던 이메일 비밀번호로 해주세요)", example = "admin@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "E-mail을 입력해주세요.")
    private String email;
    @Schema(description = "사용자 비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @Builder
    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

}
