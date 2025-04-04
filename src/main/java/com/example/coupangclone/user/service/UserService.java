package com.example.coupangclone.user.service;

import com.example.coupangclone.auth.jwt.JwtProvider;
import com.example.coupangclone.auth.service.TokenService;
import com.example.coupangclone.config.RedisUtil;
import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.dto.ErrorResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.user.dto.LoginRequestDto;
import com.example.coupangclone.user.dto.SignupRequestDto;
import com.example.coupangclone.user.dto.UserResponseDto;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;
    private final TokenService tokenService;

    @Transactional
    public ResponseEntity<?> signup(SignupRequestDto userRequestDto) {
        String email = userRequestDto.getEmail();
        String password = passwordEncoder.encode(userRequestDto.getPassword());
        String name = userRequestDto.getName();
        String tel = userRequestDto.getTel();
        String gender = userRequestDto.getGender();

        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto(ExceptionEnum.EMAIL_DUPLICATION));
        }

        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .tel(tel)
                .gender(gender)
                .role(UserRoleEnum.USER)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok(new UserResponseDto(name, "회원가입 성공"));
    }

    @Transactional
    public ResponseEntity<?> login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ErrorException(ExceptionEnum.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ErrorException(ExceptionEnum.WRONG_PASSWORD);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getName(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        redisUtil.set("RT:" + user.getId(), refreshToken, 14, TimeUnit.DAYS);

        response.addHeader(JwtProvider.AUTHORIZATION_HEADER, accessToken);
        response.addHeader(JwtProvider.REFRESH_TOKEN_HEADER, refreshToken);

        return ResponseEntity.ok(new UserResponseDto(user.getName(), "로그인 성공"));
    }

    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String accessToken = jwtProvider.resolveToken(request);
        String refreshToken = request.getHeader(JwtProvider.REFRESH_TOKEN_HEADER);

        tokenService.logout(accessToken, refreshToken);
        return ResponseEntity.ok(BasicResponseDto.addSuccess("로그아웃 되었습니다."));
    }

}
