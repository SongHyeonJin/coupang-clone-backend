package com.example.coupangclone.auth;

import com.example.coupangclone.enums.UserRoleEnum;
import io.jsonwebtoken.Claims;

public interface JwtPort {
    String createAccessToken(Long id, String email, String name, UserRoleEnum role);
    String createRefreshToken(Long id);
    boolean validationToken(String refreshToken);
    Claims getUserInfoFromToken(String refreshToken);
    long getExpiration(String accessToken);
}
