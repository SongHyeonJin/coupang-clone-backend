package com.example.coupangclone.controller.user;

import com.example.coupangclone.dto.BasicResponseDto;
import com.example.coupangclone.dto.user.LoginRequestDto;
import com.example.coupangclone.dto.user.SignupRequestDto;
import com.example.coupangclone.dto.user.UserResponseDto;
import com.example.coupangclone.entity.user.command.LoginCommand;
import com.example.coupangclone.entity.user.command.SignupCommand;
import com.example.coupangclone.jwt.JwtProvider;
import com.example.coupangclone.result.LoginResult;
import com.example.coupangclone.result.SignupResult;
import com.example.coupangclone.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입", description = "사용자가 회원가입을 합니다.")
    @PostMapping("/api/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        SignupResult result = userService.signup(requestDto.toCommand());

        return ResponseEntity.ok(new UserResponseDto(result.name(), "회원가입 성공"));
    }

    @Operation(summary = "로그인 (현재 관리자 아이디, 비밀번호를 예시로 입력되어있음)", description = "사용자가 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto requestDto,
                                    HttpServletResponse response) {
        LoginResult result = userService.login(requestDto.toCommand());

        response.addHeader(JwtProvider.AUTHORIZATION_HEADER, result.accessToken());
        response.addHeader(JwtProvider.REFRESH_TOKEN_HEADER, result.refreshToken());

        return ResponseEntity.ok(new UserResponseDto(result.name(), "로그인 성공"));
    }

    @Operation(summary = "로그아웃 (엑세스, 리프레쉬 토큰 모두 상단에 넣어주세요)", description = "사용자가 로그아웃을 합니다.")
    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String accessToken = jwtProvider.resolveToken(request);
        String refreshToken = request.getHeader(JwtProvider.REFRESH_TOKEN_HEADER);

        userService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(BasicResponseDto.addSuccess("로그아웃 되었습니다."));
    }

}
