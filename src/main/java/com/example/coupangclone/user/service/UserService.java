package com.example.coupangclone.user.service;

import com.example.coupangclone.global.dto.ErrorResponseDto;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.user.dto.SignupRequestDto;
import com.example.coupangclone.user.dto.UserResponseDto;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> signup(SignupRequestDto userRequestDto) {
        String email = userRequestDto.getEmail();
        String password = userRequestDto.getPassword();
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

}
