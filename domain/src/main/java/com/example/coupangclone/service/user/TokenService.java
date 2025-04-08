package com.example.coupangclone.service.user;

import com.example.coupangclone.auth.JwtPort;
import com.example.coupangclone.auth.RedisPort;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.user.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisPort redisPort;
    private final JwtPort jwtPort;
    private final UserRepository userRepository;

    public User verifyRefreshToken(String refreshToken) {
        if (!jwtPort.validationToken(refreshToken)) {
            throw new ErrorException(ExceptionEnum.EXPIRED_TOKEN);
        }

        Claims claims = jwtPort.getUserInfoFromToken(refreshToken);
        String userId = claims.get("userId").toString();
        String redisToken = redisPort.get("RT:" + userId);

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

        Claims claims = jwtPort.getUserInfoFromToken(refreshToken);
        Long userId = claims.get("userId", Long.class);

        redisPort.delete("RT:" + userId);

        long expiration = jwtPort.getExpiration(accessToken);
        redisPort.set("BL:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

}
