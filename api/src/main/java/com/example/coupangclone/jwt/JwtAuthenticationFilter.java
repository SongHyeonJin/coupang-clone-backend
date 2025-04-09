package com.example.coupangclone.jwt;

import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.redis.RedisAdapter;
import com.example.coupangclone.service.user.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisAdapter redisAdapter;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtProvider.resolveToken(request);

        try {
            if (token != null && jwtProvider.validationToken(token)) {
                if (redisAdapter.hasKey("BL:" + token)) {
                    throw new ErrorException(ExceptionEnum.INVALID_TOKEN);
                }
                Claims info = jwtProvider.getUserInfoFromToken(token);
                setAuthentication(info.getSubject());
            }
        } catch (ExpiredJwtException e) {
            String refreshToken = request.getHeader(JwtProvider.REFRESH_TOKEN_HEADER);

            if (refreshToken != null) {
                Claims claims = jwtProvider.getUserInfoFromToken(refreshToken);
                String userId = claims.get("userId").toString();

                User user = tokenService.verifyRefreshToken(refreshToken);
                setAuthentication(user.getEmail());

                String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getName(), user.getRole());
                response.setHeader(JwtProvider.AUTHORIZATION_HEADER, newAccessToken);

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String json = new ObjectMapper().writeValueAsString(
                        Map.of("accessToken", newAccessToken)
                );
                response.getWriter().write(json);
                return;
            }else  {
                    throw new ErrorException(ExceptionEnum.INVALID_TOKEN);
            }
        }
        filterChain.doFilter(request, response);
    }

    public void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = jwtProvider.createAuthentication(email);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

}
