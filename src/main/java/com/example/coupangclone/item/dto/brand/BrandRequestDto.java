package com.example.coupangclone.item.dto.brand;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BrandRequestDto {

    private String name;

    @Builder
    public BrandRequestDto(String name) {
        this.name = name;
    }

}
