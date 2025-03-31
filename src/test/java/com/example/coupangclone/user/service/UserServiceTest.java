package com.example.coupangclone.user.service;

import com.example.coupangclone.global.dto.ErrorResponseDto;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.user.dto.SignupRequestDto;
import com.example.coupangclone.user.dto.UserResponseDto;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("회원가입을 성공한다.")
    @Test
    void signup_success(){
        // given
        SignupRequestDto request =
                createUserDto("test@example.com", "qwer123!", "홍길동", "01012345678", "남성");

        // when
        ResponseEntity<?> response = userService.signup(request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        assertThat(response.getBody()).isInstanceOf(UserResponseDto.class);
        UserResponseDto userResponseDto = (UserResponseDto) response.getBody();
        assertThat(userResponseDto.getName()).isEqualTo(request.getName());
        assertThat(userResponseDto.getMsg()).isEqualTo("회원가입 성공");
    }

    @DisplayName("중복 이메일로 회원가입을 실패한다.")
    @Test
    void signup_duplicationEmail_fail(){
        // given
        String email = "test@example.com";
        SignupRequestDto request =
                createUserDto(email, "qwer123!", "홍길동", "01012345678", "남성");
        String password = "qwer123!";
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name("김서방")
                .tel("01032145643")
                .gender("남성")
                .role(UserRoleEnum.USER)
                .build();
        userRepository.save(user);

        // when
        ResponseEntity<?> response = userService.signup(request);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(400);

        assertThat(response.getBody()).isInstanceOf(ErrorResponseDto.class);
        ErrorResponseDto errorResponseDto = (ErrorResponseDto) response.getBody();
        assertThat(errorResponseDto.getType()).isEqualTo("USER");
        assertThat(errorResponseDto.getMsg()).isEqualTo(ExceptionEnum.EMAIL_DUPLICATION.getMsg());
    }

    private static SignupRequestDto createUserDto(String email, String password, String name, String tel, String gender) {
        return SignupRequestDto.builder()
                .email(email)
                .password(password)
                .name(name)
                .tel(tel)
                .gender(gender)
                .build();
    }

    private static User createUser(SignupRequestDto requestDto, String password, UserRoleEnum role) {
        return User.builder()
                .email(requestDto.getEmail())
                .password(password)
                .name(requestDto.getName())
                .tel(requestDto.getTel())
                .gender(requestDto.getGender())
                .role(role)
                .build();
    }

}