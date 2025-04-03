package com.example.coupangclone.item.dto.brand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "브랜드 요청 DTO")
public class BrandRequestDto {

    @Schema(description = "브랜드 이름", example = "나이키")
    private String name;

    @Builder
    public BrandRequestDto(String name) {
        this.name = name;
    }

}
