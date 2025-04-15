package com.example.coupangclone.advice;

import com.example.coupangclone.context.TokenHolder;
import com.example.coupangclone.jwt.JwtProvider;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class TokenHeaderAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getMethod() != null &&
                returnType.getMethod().getName().equals("login");
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        String access = TokenHolder.getAccessToken();
        String refresh = TokenHolder.getRefreshToken();

        if (access != null && refresh != null) {
            response.getHeaders().add(JwtProvider.AUTHORIZATION_HEADER, access);
            response.getHeaders().add(JwtProvider.REFRESH_TOKEN_HEADER, refresh);
        }

        TokenHolder.clear();

        return body;
    }
}
