package com.example.coupangclone.item.service.user;

import com.example.coupangclone.auth.RedisPort;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.entity.user.command.LoginCommand;
import com.example.coupangclone.entity.user.command.SignupCommand;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.jwt.JwtProvider;
import com.example.coupangclone.context.TokenHolder;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.result.LoginResult;
import com.example.coupangclone.result.SignupResult;
import com.example.coupangclone.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class UserServiceTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private RedisPort redisPort;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("회원가입 성공")
    @Test
    void signup_success() {
        // given
        SignupCommand command = createSignupCommand("test@example.com", "qwer123!", "홍길동", "01012345678", "남성");

        // when
        SignupResult result = userService.signup(command);

        // then
        assertThat(result.name()).isEqualTo(command.name());
    }

    @DisplayName("중복 이메일로 회원가입 실패")
    @Test
    void signup_duplicationEmail_fail() {
        // given
        String email = "test@example.com";
        userRepository.save(createUser(email, "qwer123!", "기존유저", "01000000000", "남성"));
        SignupCommand command = createSignupCommand(email, "qwer123!", "홍길동", "01012345678", "남성");

        // when & then
        assertThatThrownBy(() -> userService.signup(command))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.EMAIL_DUPLICATION.getMsg());
    }

    @DisplayName("로그인 성공")
    @Test
    void login_success() {
        // given
        String rawPassword = "qwer123!";
        String encodedPwd = passwordEncoder.encode(rawPassword);
        User user = createUser("test@example.com", encodedPwd, "홍길동", "01012345678", "남성");
        userRepository.save(user);

        LoginCommand command = new LoginCommand(user.getEmail(), rawPassword);

        // when
        LoginResult result = userService.login(command);

        // then
        assertThat(result.name()).isEqualTo(user.getName());
        assertThat(TokenHolder.getAccessToken()).isNotNull();
        assertThat(TokenHolder.getRefreshToken()).isNotNull();

        TokenHolder.clear();
    }

    @DisplayName("이메일 틀림 -> 로그인 실패")
    @Test
    void login_fail_wrongEmail() {
        // given
        String email = "test@example.com";
        String rawPassword = "qwer123!";
        String encodedPwd = passwordEncoder.encode(rawPassword);
        userRepository.save(createUser(email, encodedPwd, "홍길동", "01012345678", "남성"));

        LoginCommand wrongEmailCommand = new LoginCommand("wrong@example.com", rawPassword);

        // when & then
        assertThatThrownBy(() -> userService.login(wrongEmailCommand))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.USER_NOT_FOUND.getMsg());
    }

    @DisplayName("비밀번호 틀림 -> 로그인 실패")
    @Test
    void login_fail_wrongPassword() {
        // given
        String email = "test@example.com";
        String rawPassword = "qwer123!";
        String encodedPwd = passwordEncoder.encode(rawPassword);
        userRepository.save(createUser(email, encodedPwd, "홍길동", "01012345678", "남성"));

        LoginCommand wrongPwdCommand = new LoginCommand(email, "wrong123!");

        // when & then
        assertThatThrownBy(() -> userService.login(wrongPwdCommand))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.WRONG_PASSWORD.getMsg());
    }

    @DisplayName("로그아웃 성공")
    @Test
    void logout_success() {
        // given
        User user = createUser("test@example.com", "password123!", "테스트", "01043215678", "남성");
        userRepository.save(user);

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getName(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        redisPort.set("RT:" + user.getId(), refreshToken, 14, TimeUnit.DAYS);

        // when
        userService.logout(accessToken.substring(7), refreshToken);

        // then
        assertThat(redisPort.get("RT:" + user.getId())).isNull();
        assertThat(redisPort.get("BL:" + accessToken.substring(7))).isEqualTo("logout");
    }

    @DisplayName("잘못된 토큰으로 로그아웃 실패")
    @Test
    void logout_invalidTokens() {
        // when & then
        assertThatThrownBy(() -> userService.logout(null, null))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.INVALID_TOKEN.getMsg());
    }

    private SignupCommand createSignupCommand(String email, String password, String name, String tel, String gender) {
        return new SignupCommand(email, password, name, tel, gender);
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
