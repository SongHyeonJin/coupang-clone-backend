package com.example.coupangclone.result;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResult(
        Long orderId,
        String status,
        String statusDescription,
        int totalPrice,
        LocalDateTime orderedAt,
        List<OrderItemResult> items
) {
}
