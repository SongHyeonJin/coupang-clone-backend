package com.example.coupangclone.jwt;

import com.example.coupangclone.auth.JwtPort;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.security.userdetails.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider implements JwtPort {

    private final UserDetailsServiceImpl userDetailsService;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";
    public static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TIME = Duration.ofMinutes(15).toMillis();
    private static final long REFRESH_TOKEN_TIME = Duration.ofDays(14).toMillis();
    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public String createAccessToken(Long id, String email, String name, UserRoleEnum role) {
        Date issuedAt = new Date();

        return BEARER_PREFIX + Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(email)
                .claim(AUTHORIZATION_KEY, role)
                .claim("userId", id)
                .claim("name", name)
                .setExpiration(new Date(issuedAt.getTime() + ACCESS_TOKEN_TIME))
                .setIssuedAt(issuedAt)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    @Override
    public String createRefreshToken(Long id) {
        Date issuedAt = new Date();

        return Jwts.builder()
                .setSubject("refreshToken")
                .claim("userId", id)
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + REFRESH_TOKEN_TIME))
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public boolean validationToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new ErrorException(ExceptionEnum.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new ErrorException(ExceptionEnum.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new ErrorException(ExceptionEnum.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new ErrorException(ExceptionEnum.WRONG_TOKEN);
        }
    }

    public long getExpiration(String token) {
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Authentication createAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}
