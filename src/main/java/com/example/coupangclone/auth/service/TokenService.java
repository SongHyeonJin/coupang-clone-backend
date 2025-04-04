package com.example.coupangclone.auth.service;

import com.example.coupangclone.auth.jwt.JwtProvider;
import com.example.coupangclone.config.RedisUtil;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisUtil redisUtil;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public User verifyRefreshToken(String refreshToken) {
        if (!jwtProvider.validationToken(refreshToken)) {
            throw new ErrorException(ExceptionEnum.EXPIRED_TOKEN);
        }

        Claims claims = jwtProvider.getUserInfoFromToken(refreshToken);
        String userId = claims.get("userId").toString();
        String redisToken = redisUtil.get("RT:" + userId);

        if (!refreshToken.equals(redisToken)) {
            throw new ErrorException(ExceptionEnum.INVALID_TOKEN);
        }

        return userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ErrorException(ExceptionEnum.USER_NOT_FOUND));
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken == null || refreshToken == null) {
            throw new ErrorException(ExceptionEnum.INVALID_TOKEN);
        }

        Claims claims = jwtProvider.getUserInfoFromToken(refreshToken);
        Long userId = claims.get("userId", Long.class);

        redisUtil.delete("RT:" + userId);

        long expiration = jwtProvider.getExpiration(accessToken);
        redisUtil.set("BL:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

}
