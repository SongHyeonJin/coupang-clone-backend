package com.example.coupangclone.item.dto.category;

import com.example.coupangclone.item.enums.ItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "카테고리 요청 DTO")
public class CategoryRequestDto {
    @Schema(description = "카테고리 이름", example = "패션의류")
    private String name;

    @Schema(description = "아이템 유형", example = "THING")
    private ItemTypeEnum type;

    @Schema(description = "부모 카테고리 (최상위면 null)")
    private Long parentId;

    @Builder
    public CategoryRequestDto(String name, ItemTypeEnum type, Long parentId) {
        this.name = name;
        this.type = type;
        this.parentId = parentId;
    }

}
