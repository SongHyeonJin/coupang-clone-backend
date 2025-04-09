package com.example.coupangclone.entity.item.command;

import lombok.Builder;

@Builder
public record ItemCommand(
        String name,
        int weight,
        String content,
        int price,
        int sale,
        int saleCnt,
        int deliveryTime,
        int deliveryPrice,
        Long categoryId,
        Long brandId
) {
}
