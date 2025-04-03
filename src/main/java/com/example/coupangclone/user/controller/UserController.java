package com.example.coupangclone.user.controller;

import com.example.coupangclone.user.dto.LoginRequestDto;
import com.example.coupangclone.user.dto.SignupRequestDto;
import com.example.coupangclone.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "user-controller", description = "회원가입 및 로그인 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "사용자가 회원가입을 합니다.")
    @PostMapping("/api/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        return userService.signup(requestDto);
    }

    @Operation(summary = "로그인 (현재 관리자 아이디, 비밀번호를 예시로 입력되어있음)", description = "사용자가 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto requestDto,
                                    HttpServletResponse response) {
        return userService.login(requestDto, response);
    }

}
