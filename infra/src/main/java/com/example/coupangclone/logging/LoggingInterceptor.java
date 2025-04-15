package com.example.coupangclone.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = MDC.get(TRACE_ID);
        String method = request.getMethod();
        String uri = request.getRequestURI();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            MDC.put("userId", auth.getName());
        }

        String logMessage;

        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            logMessage = logMultipartRequest(multipartRequest, traceId);
        } else if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            logMessage = "[Body was logged by Filter]";
        } else {
            logMessage = "[Unsupported or unknown content type: " + request.getContentType() + "]";
        }

        log.info("✅ Multipart-Request: [{}] {} {}\n{}", traceId, method, uri, logMessage);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String traceId = MDC.get(TRACE_ID);

        if (response instanceof ContentCachingResponseWrapper wrapper) {
            String responseBody = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.info("✅ ResponseBody: [{}] {} {}\nBody: {}", traceId, response.getStatus(), response.getContentType(), responseBody);
        } else {
            log.warn("Response body unavailable — wrapper missing");
        }

        MDC.clear();
    }

    private String logMultipartRequest(MultipartHttpServletRequest multipartRequest, String traceId) {
        Map<String, String> paramMap = multipartRequest.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().length > 0 ? entry.getValue()[0] : ""
                ));

        boolean hasImage = multipartRequest.getMultiFileMap().values().stream()
                .flatMap(List::stream)
                .peek(file -> log.debug("💾 file key: {}, name: {}, size: {}, contentType: {}, isEmpty: {}",
                        file.getName(), file.getOriginalFilename(), file.getSize(), file.getContentType(), file.isEmpty()))
                .anyMatch(file ->
                        !file.isEmpty() && isImageFile(file.getOriginalFilename())
                );

        return String.format("Form Params: %s\nImage Included: %s", paramMap, hasImage);
    }

    private boolean isImageFile(String filename) {
        if (filename == null) return false;
        String lowered = filename.toLowerCase();
        return lowered.endsWith(".jpg") || lowered.endsWith(".jpeg") ||
                lowered.endsWith(".png") || lowered.endsWith(".gif") ||
                lowered.endsWith(".bmp") || lowered.endsWith(".webp");
    }
}
