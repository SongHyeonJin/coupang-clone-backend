package com.example.coupangclone.item.dto.category;

import com.example.coupangclone.item.entity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryRequestDto {

    private String name;
    private Category parent;

}
