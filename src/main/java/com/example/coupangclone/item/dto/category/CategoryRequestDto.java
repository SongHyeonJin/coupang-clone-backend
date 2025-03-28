package com.example.coupangclone.item.dto.category;

import com.example.coupangclone.item.entity.Category;
import com.example.coupangclone.item.enums.ItemTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryRequestDto {

    private String name;
    private ItemTypeEnum type;
    private Category parent;

}
