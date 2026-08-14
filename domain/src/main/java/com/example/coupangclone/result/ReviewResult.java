package com.example.coupangclone.result;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResult(
        Long reviewId,
        Long itemId,
        String userName,
        String content,
        Double rating,
        LocalDateTime createdAt
) {
}
