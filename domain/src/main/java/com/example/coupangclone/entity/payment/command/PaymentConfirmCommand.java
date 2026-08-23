package com.example.coupangclone.entity.payment.command;

import lombok.Builder;

@Builder
public record PaymentConfirmCommand(
        Long orderId,
        String paymentKey,
        Integer amount
) {
}
