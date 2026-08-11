package com.example.coupangclone.dto.order;

import com.example.coupangclone.entity.order.command.OrderItemCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "주문 상품 요청 DTO")
public class OrderItemRequestDto {

    @Schema(description = "상품 ID", example = "1")
    private Long itemId;

    @Schema(description = "주문 수량", example = "2")
    private Integer quantity;

    public OrderItemCommand toCommand() {
        return new OrderItemCommand(itemId, quantity);
    }
}
