package com.example.coupangclone.util;

import com.example.coupangclone.entity.order.Order;
import com.example.coupangclone.entity.order.OrderItem;
import com.example.coupangclone.result.OrderItemResult;
import com.example.coupangclone.result.OrderResult;

public class OrderMapper {

    public static OrderResult toResult(Order order) {
        return OrderResult.builder()
                .orderId(order.getId())
                .status(order.getStatus().name())
                .statusDescription(order.getStatus().getDescription())
                .totalPrice(order.getTotalPrice())
                .orderedAt(order.getCreatedAt())
                .items(order.getOrderItems().stream()
                        .map(OrderMapper::toItemResult)
                        .toList())
                .build();
    }

    private static OrderItemResult toItemResult(OrderItem orderItem) {
        return OrderItemResult.builder()
                .orderItemId(orderItem.getId())
                .itemId(orderItem.getItem().getId())
                .itemName(orderItem.getItem().getName())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .build();
    }

}
