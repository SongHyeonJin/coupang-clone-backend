package com.example.coupangclone.dto.payment;

import com.example.coupangclone.result.PaymentResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "결제 응답 DTO")
public class PaymentResponseDto {

    private Long paymentId;
    private Long orderId;
    private String status;
    private String statusDescription;
    private Integer amount;
    private String method;

    public PaymentResponseDto(Long paymentId, Long orderId, String status, String statusDescription,
                               Integer amount, String method) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
        this.statusDescription = statusDescription;
        this.amount = amount;
        this.method = method;
    }

    public static PaymentResponseDto from(PaymentResult result) {
        return new PaymentResponseDto(
                result.paymentId(),
                result.orderId(),
                result.status(),
                result.statusDescription(),
                result.amount(),
                result.method()
        );
    }
}
