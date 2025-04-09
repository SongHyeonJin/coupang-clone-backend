package com.example.coupangclone.item.service.user;

import com.example.coupangclone.auth.RedisPort;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.jwt.JwtProvider;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.service.user.TokenService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class TokenServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private RedisPort redisPort;
    @Autowired
    private TokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser("test@example.com", "password", "테스트", "0101111222", "남성");
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("RefreshToken이 유효한 경우 사용자 정보를 반환한다.")
    @Test
    void verifyRefreshToken_success() {
        // Given
        String refreshToken = jwtProvider.createRefreshToken(testUser.getId());
        redisPort.set("RT:" + testUser.getId(), refreshToken, 14, TimeUnit.DAYS);

        // When
        User result = tokenService.verifyRefreshToken(refreshToken);

        // Then
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
    }

    @DisplayName("유효하지 않은 RefreshToken의 경우 예외 발생한다.")
    @Test
    void verifyRefreshToken_invalid() {
        // Given
        String invalidRefreshToken = "invalid_refresh_token";

        // When & Then
        assertThatThrownBy(() -> tokenService.verifyRefreshToken(invalidRefreshToken))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.INVALID_TOKEN.getMsg());
    }

    @DisplayName("로그아웃 시 AccessToken과 RefreshToken을 처리한다.")
    @Test
    void logout_success() {
        // Given
        String accessToken = jwtProvider.createAccessToken(testUser.getId(), testUser.getEmail(), testUser.getName(), testUser.getRole());
        String refreshToken = jwtProvider.createRefreshToken(testUser.getId());
        redisPort.set("RT:" + testUser.getId(), refreshToken, 14, TimeUnit.DAYS);

        // When
        tokenService.logout(accessToken.substring(7), refreshToken);

        // Then
        Assertions.assertThat(redisPort.get("RT:" + testUser.getId())).isNull();
        Assertions.assertThat(redisPort.get("BL:" + accessToken.substring(7))).isEqualTo("logout");
    }

    @DisplayName("로그아웃 시 토큰 값이 없다면 예외가 발생한다.")
    @Test
    void logout_invalidTokens() {
        // Given
        String accessToken = null;
        String refreshToken = null;

        // When & Then
        assertThatThrownBy(() -> tokenService.logout(accessToken, refreshToken))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.INVALID_TOKEN.getMsg());
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