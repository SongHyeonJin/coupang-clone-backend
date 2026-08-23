package com.example.coupangclone.enums;

public enum OrderStatus {
    PAYMENT_PENDING("결제대기"),
    PAID("결제완료"),
    SHIPPING("배송중"),
    DELIVERED("배송완료"),
    CANCELED("취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
