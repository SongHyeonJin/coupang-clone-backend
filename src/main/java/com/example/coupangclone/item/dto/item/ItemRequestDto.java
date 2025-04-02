package com.example.coupangclone.item.dto.item;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemRequestDto {

    private String name;
    private int weight;
    private String content;
    private int price;
    private int sale;
    private int saleCnt;
    private int deliveryTime;
    private int deliveryPrice;
    private Long categoryId;
    private Long brandId;

    @Builder
    public ItemRequestDto(String name, int weight, String content, int price, int sale, int saleCnt,
                          int deliveryTime, int deliveryPrice, Long categoryId, Long brandId) {
        this.name = name;
        this.weight = weight;
        this.content = content;
        this.price = price;
        this.sale = sale;
        this.saleCnt = saleCnt;
        this.deliveryTime = deliveryTime;
        this.deliveryPrice = deliveryPrice;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }
}
