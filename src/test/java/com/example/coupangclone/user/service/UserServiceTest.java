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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

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
        User user = createUser(email, password, "김서방", "01043215678", "남성");
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

    @DisplayName("회원인 사람이 로그인을 성공한다.")
    @Test
    void login_success(){
        // given
        LoginRequestDto requestDto = createLoginDto("test@example.com", "qwer123!");
        String encodedPwd = passwordEncoder.encode(requestDto.getPassword());
        User user = createUser(requestDto.getEmail(), encodedPwd, "홍길동", "01012345678", "남성");
        userRepository.save(user);


        // when
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        String token = jwtProvider.createToken(user.getId(), user.getEmail(), user.getName(), user.getRole());
        httpServletResponse.addHeader(JwtProvider.AUTHORIZATION_HEADER, token);

        ResponseEntity<?> response = userService.login(requestDto, httpServletResponse);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(UserResponseDto.class);

        UserResponseDto userResponseDto = (UserResponseDto) response.getBody();
        assertThat(userResponseDto.getName()).isEqualTo(user.getName());
        assertThat(userResponseDto.getMsg()).isEqualTo("로그인 성공");
    }

    @DisplayName("이메일을 잘못 입력할 시 로그인을 실패한다.")
    @Test
    void login_fail_wrongEmail(){
        // given
        LoginRequestDto requestDto = createLoginDto("test@example.com", "qwer123!");
        String encodedPwd = passwordEncoder.encode(requestDto.getPassword());
        User user = createUser(requestDto.getEmail(), encodedPwd, "홍길동", "01012345678", "남성");
        userRepository.save(user);

        requestDto.updateEmail("test1@example.com");

        // when // then
        assertThatThrownBy(() -> userService.login(requestDto, null))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.USER_NOT_FOUND.getMsg());
    }

    @DisplayName("비밀번호를 잘못 입력 시 로그인을 실패한다.")
    @Test
    void login_fail_wrongPassword(){
        // given
        LoginRequestDto requestDto = createLoginDto("test@example.com", "qwer123!");
        String encodedPwd = passwordEncoder.encode(requestDto.getPassword());
        User user = createUser(requestDto.getEmail(), encodedPwd, "홍길동", "01012345678", "남성");
        userRepository.save(user);

        requestDto.updatePassword("asdf123!");

        // when // then
        assertThatThrownBy(() -> userService.login(requestDto, null))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.WRONG_PASSWORD.getMsg());
    }

    private SignupRequestDto createUserDto(String email, String password, String name, String tel, String gender) {
        return SignupRequestDto.builder()
                .email(email)
                .password(password)
                .name(name)
                .tel(tel)
                .gender(gender)
                .build();
    }

    private LoginRequestDto createLoginDto(String email, String password) {
        return LoginRequestDto.builder()
                .email(email)
                .password(password)
                .build();
    }

    private User createUser(String email, String password, String name, String tel, String gender) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .tel(tel)
                .gender(gender)
                .role(UserRoleEnum.USER)
                .build();
    }

}