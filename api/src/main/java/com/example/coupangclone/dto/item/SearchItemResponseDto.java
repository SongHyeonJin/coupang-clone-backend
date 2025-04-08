package com.example.coupangclone.dto.item;

import com.example.coupangclone.result.SearchItemResult;
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

    public SearchItemResponseDto(Page<ItemResponseDto> items, List<String> relatedKeywords) {
        this.items = items;
        this.relatedKeywords = relatedKeywords;
    }

    public static SearchItemResponseDto from(SearchItemResult result) {
        Page<ItemResponseDto> itemDtos = result.items().map(ItemResponseDto::from);
        return new SearchItemResponseDto(itemDtos, result.relatedKeywords());
    }

}
