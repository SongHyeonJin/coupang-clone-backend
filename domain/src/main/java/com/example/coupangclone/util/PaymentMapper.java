package com.example.coupangclone.util;

import com.example.coupangclone.entity.payment.Payment;
import com.example.coupangclone.result.PaymentResult;

public class PaymentMapper {

    public static PaymentResult toResult(Payment payment) {
        return PaymentResult.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .status(payment.getStatus().name())
                .statusDescription(payment.getStatus().getDescription())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .build();
    }

}
