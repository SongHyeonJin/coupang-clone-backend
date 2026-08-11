package com.example.coupangclone.controller.order;

import com.example.coupangclone.dto.BasicResponseDto;
import com.example.coupangclone.dto.order.OrderRequestDto;
import com.example.coupangclone.dto.order.OrderResponseDto;
import com.example.coupangclone.result.OrderResult;
import com.example.coupangclone.security.userdetails.UserDetailsImpl;
import com.example.coupangclone.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "주문 API", description = "주문 생성, 조회 및 취소 API")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "상품 목록으로 주문을 생성하고 재고를 차감합니다.")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto requestDto,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResult result = orderService.createOrder(requestDto.toCommand(), userDetails.getUser());
        return ResponseEntity.ok(OrderResponseDto.from(result));
    }

    @Operation(summary = "주문 목록 조회", description = "본인의 주문 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getOrders(@PageableDefault(size = 10) Pageable pageable,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Page<OrderResponseDto> response = orderService.getOrders(pageable, userDetails.getUser())
                .map(OrderResponseDto::from);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주문 단건 조회", description = "본인의 주문을 단건 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResult result = orderService.getOrder(orderId, userDetails.getUser());
        return ResponseEntity.ok(OrderResponseDto.from(result));
    }

    @Operation(summary = "주문 취소", description = "본인의 주문을 취소하고 재고를 복구합니다.")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<BasicResponseDto> cancelOrder(@PathVariable Long orderId,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        orderService.cancelOrder(orderId, userDetails.getUser());
        return ResponseEntity.ok(BasicResponseDto.addSuccess("주문이 취소되었습니다."));
    }

}
