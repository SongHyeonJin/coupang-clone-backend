package com.example.coupangclone.result;

import lombok.Builder;

@Builder
public record ReviewSummaryResult(
        Long itemId,
        double averageRating,
        long reviewCount
) {
}
