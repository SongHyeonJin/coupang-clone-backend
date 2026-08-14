package com.example.coupangclone.entity.review.command;

import lombok.Builder;

@Builder
public record ReviewWriteCommand(
        Long orderItemId,
        String content,
        Double rating
) {
}
