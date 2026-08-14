package com.example.coupangclone.jwt;

import com.example.coupangclone.dto.ErrorResponseDto;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.redis.RedisAdapter;
import com.example.coupangclone.service.user.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
        } catch (ErrorException e) {
            if (e.getExceptionEnum() == ExceptionEnum.EXPIRED_TOKEN) {
                String refreshToken = request.getHeader(JwtProvider.REFRESH_TOKEN_HEADER);

                if (refreshToken != null) {
                    if (renewAccessToken(refreshToken, response)) {
                        return;
                    }
                    writeErrorResponse(response, ExceptionEnum.INVALID_TOKEN);
                    return;
                }
            }
            writeErrorResponse(response, e.getExceptionEnum());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean renewAccessToken(String refreshToken, HttpServletResponse response) throws IOException {
        try {
            User user = tokenService.verifyRefreshToken(refreshToken);
            setAuthentication(user.getEmail());

            String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getName(), user.getRole());
            response.setHeader(JwtProvider.AUTHORIZATION_HEADER, newAccessToken);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            String json = new ObjectMapper().writeValueAsString(
                    Map.of("accessToken", newAccessToken)
            );
            response.getWriter().write(json);
            return true;
        } catch (ErrorException e) {
            return false;
        }
    }

    private void writeErrorResponse(HttpServletResponse response, ExceptionEnum exceptionEnum) throws IOException {
        response.setStatus(exceptionEnum.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String json = new ObjectMapper().writeValueAsString(new ErrorResponseDto(exceptionEnum));
        response.getWriter().write(json);
    }

    public void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = jwtProvider.createAuthentication(email);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

}
