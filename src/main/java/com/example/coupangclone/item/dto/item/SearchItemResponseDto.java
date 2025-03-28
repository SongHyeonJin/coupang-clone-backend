package com.example.coupangclone.item.dto.item;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class SearchItemResponseDto {

    private Page<ItemResponseDto> items;
    List<String> relatedKeywords;

}
