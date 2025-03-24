package com.example.coupangclone.user.service;

import com.example.coupangclone.auth.jwt.JwtProvider;
import com.example.coupangclone.global.dto.ErrorResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.user.dto.LoginRequestDto;
import com.example.coupangclone.user.dto.SignupRequestDto;
import com.example.coupangclone.user.dto.UserResponseDto;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

        User uesr = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .tel(tel)
                .gender(gender)
                .role(UserRoleEnum.USER)
                .build();
        userRepository.save(uesr);
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

        String token = jwtProvider.createToken(user.getId(), user.getEmail(), user.getName(), user.getRole());
        response.addHeader(JwtProvider.AUTHORIZATION_HEADER, token);

        return ResponseEntity.ok(new UserResponseDto(user.getName(), "로그인 성공"));
    }

}
