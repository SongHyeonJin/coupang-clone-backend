package com.example.coupangclone.entity.order.command;

import lombok.Builder;

@Builder
public record OrderItemCommand(
        Long itemId,
        Integer quantity
) {
}
