package com.example.coupangclone.auth;

public interface PaymentGatewayPort {

    String confirm(String paymentKey, String orderId, int amount);

    void cancel(String paymentKey, String cancelReason);

}
