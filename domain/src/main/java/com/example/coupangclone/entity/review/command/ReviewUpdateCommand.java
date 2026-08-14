package com.example.coupangclone.entity.review.command;

import lombok.Builder;

@Builder
public record ReviewUpdateCommand(
        String content,
        Double rating
) {
}
