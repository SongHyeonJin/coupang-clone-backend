package com.example.coupangclone.dto.order;

import com.example.coupangclone.result.OrderResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Schema(description = "주문 응답 DTO")
public class OrderResponseDto {

    private Long orderId;
    private String status;
    private String statusDescription;
    private int totalPrice;
    private LocalDateTime orderedAt;
    private List<OrderItemResponseDto> items;

    public OrderResponseDto(Long orderId, String status, String statusDescription, int totalPrice,
                             LocalDateTime orderedAt, List<OrderItemResponseDto> items) {
        this.orderId = orderId;
        this.status = status;
        this.statusDescription = statusDescription;
        this.totalPrice = totalPrice;
        this.orderedAt = orderedAt;
        this.items = items;
    }

    public static OrderResponseDto from(OrderResult result) {
        return new OrderResponseDto(
                result.orderId(),
                result.status(),
                result.statusDescription(),
                result.totalPrice(),
                result.orderedAt(),
                result.items().stream().map(OrderItemResponseDto::from).toList()
        );
    }
}
