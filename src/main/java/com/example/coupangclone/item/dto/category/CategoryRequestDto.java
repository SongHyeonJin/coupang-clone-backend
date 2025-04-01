package com.example.coupangclone.item.dto.category;

import com.example.coupangclone.item.entity.Category;
import com.example.coupangclone.item.enums.ItemTypeEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryRequestDto {

    private String name;
    private ItemTypeEnum type;
    private Category parent;

    @Builder
    public CategoryRequestDto(String name, ItemTypeEnum type, Category parent) {
        this.name = name;
        this.type = type;
        this.parent = parent;
    }

}
