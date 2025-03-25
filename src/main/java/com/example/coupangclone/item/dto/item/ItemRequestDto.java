package com.example.coupangclone.item.dto.item;

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

}
