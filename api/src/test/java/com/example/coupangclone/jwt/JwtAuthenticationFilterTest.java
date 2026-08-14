package com.example.coupangclone.jwt;

import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.redis.RedisAdapter;
import com.example.coupangclone.service.user.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    private final RedisAdapter redisAdapter = mock(RedisAdapter.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider, redisAdapter, tokenService);

    @DisplayName("만료된 액세스 토큰만 있고 리프레시 토큰이 없으면 예외를 던지지 않고 401 JSON으로 응답한다.")
    @Test
    void expiredToken_withoutRefreshToken_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProvider.AUTHORIZATION_HEADER, "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtProvider.resolveToken(request)).thenReturn("expired-token");
        when(jwtProvider.validationToken("expired-token")).thenThrow(new ErrorException(ExceptionEnum.EXPIRED_TOKEN));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(ExceptionEnum.EXPIRED_TOKEN.getStatus());
        assertThat(response.getContentAsString()).contains(ExceptionEnum.EXPIRED_TOKEN.getMsg());
        assertThat(chain.getRequest()).isNull();
    }

    @DisplayName("만료된 액세스 토큰 + 유효한 리프레시 토큰이면 새 액세스 토큰을 발급하고 체인을 진행하지 않는다.")
    @Test
    void expiredToken_withValidRefreshToken_renewsAccessToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProvider.AUTHORIZATION_HEADER, "Bearer expired-token");
        request.addHeader(JwtProvider.REFRESH_TOKEN_HEADER, "refresh-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        User user = User.builder()
                .email("test@example.com").password("pw").name("홍길동").tel("01000000000")
                .gender("남성").role(UserRoleEnum.USER).build();

        when(jwtProvider.resolveToken(request)).thenReturn("expired-token");
        when(jwtProvider.validationToken("expired-token")).thenThrow(new ErrorException(ExceptionEnum.EXPIRED_TOKEN));
        when(tokenService.verifyRefreshToken("refresh-token")).thenReturn(user);
        when(jwtProvider.createAuthentication(user.getEmail()))
                .thenReturn(new UsernamePasswordAuthenticationToken(user.getEmail(), null));
        when(jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getName(), user.getRole()))
                .thenReturn("Bearer new-access-token");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(JwtProvider.AUTHORIZATION_HEADER)).isEqualTo("Bearer new-access-token");
        assertThat(response.getContentAsString()).contains("new-access-token");
        assertThat(chain.getRequest()).isNull();
    }

    @DisplayName("서명이 유효하지 않은 토큰은 예외를 던지지 않고 상태 코드에 맞는 JSON으로 응답한다.")
    @Test
    void invalidToken_returnsErrorResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtProvider.AUTHORIZATION_HEADER, "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtProvider.resolveToken(request)).thenReturn("bad-token");
        when(jwtProvider.validationToken("bad-token")).thenThrow(new ErrorException(ExceptionEnum.INVALID_TOKEN));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(ExceptionEnum.INVALID_TOKEN.getStatus());
        assertThat(chain.getRequest()).isNull();
    }
}
