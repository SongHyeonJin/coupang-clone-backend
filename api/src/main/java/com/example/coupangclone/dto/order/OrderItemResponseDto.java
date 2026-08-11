package com.example.coupangclone.dto.order;

import com.example.coupangclone.result.OrderItemResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "주문 상품 응답 DTO")
public class OrderItemResponseDto {

    private Long itemId;
    private String itemName;
    private int price;
    private int quantity;

    public OrderItemResponseDto(Long itemId, String itemName, int price, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public static OrderItemResponseDto from(OrderItemResult result) {
        return new OrderItemResponseDto(result.itemId(), result.itemName(), result.price(), result.quantity());
    }
}
