package com.example.coupangclone.auth.jwt;

import com.example.coupangclone.auth.userdetails.UserDetailsServiceImpl;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.user.enums.UserRoleEnum;
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
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final UserDetailsServiceImpl userDetailsService;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long TOKEN_TIME = 60 * 60 * 1000L;
    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Long id, String email, String name, UserRoleEnum role) {
        Date issuedAt = new Date();

        return BEARER_PREFIX + Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(email)
                .claim(AUTHORIZATION_KEY, role)
                .claim("userId", id)
                .claim("name", name)
                .setExpiration(new Date(issuedAt.getTime() + TOKEN_TIME))
                .setIssuedAt(issuedAt)
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

    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Authentication createAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}
