package com.example.coupangclone.entity.order.command;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderCommand(
        List<OrderItemCommand> items
) {
}
