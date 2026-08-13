package com.example.coupangclone.dto.payment;

import com.example.coupangclone.entity.payment.command.PaymentConfirmCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "결제 승인 요청 DTO")
public class PaymentConfirmRequestDto {

    @Schema(description = "주문 ID")
    private Long orderId;

    @Schema(description = "토스페이먼츠 paymentKey")
    private String paymentKey;

    @Schema(description = "결제 금액")
    private Integer amount;

    public PaymentConfirmCommand toCommand() {
        return PaymentConfirmCommand.builder()
                .orderId(orderId)
                .paymentKey(paymentKey)
                .amount(amount)
                .build();
    }
}
