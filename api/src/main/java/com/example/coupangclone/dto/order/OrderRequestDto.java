package com.example.coupangclone.dto.order;

import com.example.coupangclone.entity.order.command.OrderCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "주문 생성 요청 DTO")
public class OrderRequestDto {

    @Schema(description = "주문할 상품 목록")
    private List<OrderItemRequestDto> items;

    public OrderCommand toCommand() {
        return new OrderCommand(items.stream().map(OrderItemRequestDto::toCommand).toList());
    }
}
