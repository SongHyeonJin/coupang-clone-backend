package com.example.coupangclone.entity.payment;

import com.example.coupangclone.entity.base.Timestamped;
import com.example.coupangclone.entity.order.Order;
import com.example.coupangclone.enums.PaymentStatus;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String paymentKey;

    private String method;

    @Builder
    public Payment(Order order, Integer amount) {
        this.order = order;
        this.amount = amount;
        this.status = PaymentStatus.READY;
    }

    public void markDone(String paymentKey, String method) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.status = PaymentStatus.DONE;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markCanceled() {
        if (this.status != PaymentStatus.DONE) {
            throw new ErrorException(ExceptionEnum.PAYMENT_CANCEL_FAILED);
        }
        this.status = PaymentStatus.CANCELED;
    }

}
