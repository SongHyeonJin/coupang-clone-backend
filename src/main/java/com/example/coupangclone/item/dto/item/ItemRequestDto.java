package com.example.coupangclone.item.dto.item;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemRequestDto {

    private String name;
    private String content;
    private int price;
    private int sale;
    private int stock;
    private Long categoryId;
    private Long brandId;

}
