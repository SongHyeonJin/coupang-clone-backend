package com.example.coupangclone.dto.user;

import com.example.coupangclone.entity.user.command.SignupCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignupRequestDto {

    @Schema(description = "사용자 이메일", example = "test@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Pattern(regexp = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "E-mail을 입력해주세요.")
    private String email;

    @Schema(description = "사용자 비밀번호 (8~15자, 대소문자+숫자+특수문자 포함)", example = "Password123!")
    @Size(min = 8, max = 15, message = "비밀번호는 최소 8자에서 15자 사이로만 가능합니다.")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])(?=.*\\p{Punct}).+$", message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야됩니다.")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "성함을 입력해주세요.")
    private String name;

    @Schema(description = "사용자 전화번호", example = "01012345678")
    @NotBlank(message = "전화번호를 입력해주세요.")
    private String tel;

    @Schema(description = "성별 (선택)", example = "남성")
    private String gender;

    @Builder
    public SignupRequestDto(String email, String password, String name, String tel, String gender) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.tel = tel;
        this.gender = gender;
    }

    public SignupCommand toCommand() {
        return new SignupCommand(email, password, name, tel, gender);
    }
}
