package com.example.coupangclone.enums;

public enum PaymentStatus {
    READY("결제대기"),
    DONE("결제완료"),
    CANCELED("결제취소"),
    FAILED("결제실패");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
