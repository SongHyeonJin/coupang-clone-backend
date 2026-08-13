package com.example.coupangclone.payment.support;

import com.example.coupangclone.auth.PaymentGatewayPort;

public class FakePaymentGateway implements PaymentGatewayPort {

    @Override
    public String confirm(String paymentKey, String orderId, int amount) {
        return "카드";
    }

    @Override
    public void cancel(String paymentKey, String cancelReason) {
    }

}
