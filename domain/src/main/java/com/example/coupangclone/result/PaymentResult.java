package com.example.coupangclone.result;

import lombok.Builder;

@Builder
public record PaymentResult(
        Long paymentId,
        Long orderId,
        String status,
        String statusDescription,
        Integer amount,
        String method
) {
}
