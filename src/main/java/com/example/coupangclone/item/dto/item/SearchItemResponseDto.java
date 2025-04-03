package com.example.coupangclone.item.dto.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@Schema(description = "검색 상품 응답 DTO")
public class SearchItemResponseDto {

    private Page<ItemResponseDto> items;
    private List<String> relatedKeywords;

}
