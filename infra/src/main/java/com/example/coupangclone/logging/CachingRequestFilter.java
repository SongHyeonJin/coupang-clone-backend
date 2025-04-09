package com.example.coupangclone.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class CachingRequestFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID, traceId);
        wrappedRequest.setAttribute(TRACE_ID, traceId);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            logRequestBody(wrappedRequest, traceId);
        } finally {
            wrappedResponse.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logRequestBody(ContentCachingRequestWrapper request, String traceId) {
        MDC.put(TRACE_ID, traceId);

        String contentType = request.getContentType();
        String authorization = request.getHeader("Authorization");

        if (authorization != null) {
            String masked = authorization.length() > 10 ? authorization.substring(0, 10) + "..." : authorization;
            MDC.put("authorization", masked);
        }

        if (request.getContentLength() == 0) {
            log.info("✅ RequestBody: [{}] empty body", traceId);
            return;
        }

        if (contentType != null && contentType.contains("application/json")) {
            String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            String maskedBody = maskSensitiveFields(body);
            log.info("✅ RequestBody: [{}] {}", traceId, maskedBody);
        } else {
            log.info("✅ RequestBody: [{}] Unsupported or missing content-type: {}", traceId, contentType);
        }
    }

    private String maskSensitiveFields(String body) {
        return body.replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]*(\")", "$1***$2");
    }
}
