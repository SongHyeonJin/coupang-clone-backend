package com.example.coupangclone.result;

import lombok.Builder;

@Builder
public record OrderItemResult(
        Long orderItemId,
        Long itemId,
        String itemName,
        int price,
        int quantity
) {
}
