package com.example.coupangclone.controller.payment;

import com.example.coupangclone.dto.payment.PaymentConfirmRequestDto;
import com.example.coupangclone.dto.payment.PaymentResponseDto;
import com.example.coupangclone.result.PaymentResult;
import com.example.coupangclone.security.userdetails.UserDetailsImpl;
import com.example.coupangclone.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "결제 API", description = "토스페이먼츠 결제 승인 API")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 승인", description = "토스페이먼츠 결제 승인을 검증하고 주문을 결제완료 처리합니다.")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponseDto> confirm(@RequestBody PaymentConfirmRequestDto requestDto,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        PaymentResult result = paymentService.confirm(requestDto.toCommand(), userDetails.getUser());
        return ResponseEntity.ok(PaymentResponseDto.from(result));
    }

}
