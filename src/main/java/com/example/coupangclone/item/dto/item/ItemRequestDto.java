package com.example.coupangclone.item.dto.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "상품 등록 요청 DTO")
public class ItemRequestDto {

    @Schema(description = "상품 이름", example = "나이키 에어맥스")
    private String name;

    @Schema(description = "무게 단위로 팔 때 입력", example = "0")
    private int weight;

    @Schema(description = "상품 설명", example = "이뻐요")
    private String content;

    @Schema(description = "정가", example = "140000")
    private int price;

    @Schema(description = "판매가", example = "123400")
    private int sale;

    @Schema(description = "판매 수량", example = "1")
    private int saleCnt;

    @Schema(description = "배송 소요 시간 (일 단위)", example = "1")
    private int deliveryTime;

    @Schema(description = "배송비", example = "0")
    private int deliveryPrice;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "브랜드 ID (브랜드 없으면 null)", example = "1")
    private Long brandId;

    @Builder
    public ItemRequestDto(String name, int weight, String content, int price, int sale, int saleCnt,
                          int deliveryTime, int deliveryPrice, Long categoryId, Long brandId) {
        this.name = name;
        this.weight = weight;
        this.content = content;
        this.price = price;
        this.sale = sale;
        this.saleCnt = saleCnt;
        this.deliveryTime = deliveryTime;
        this.deliveryPrice = deliveryPrice;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }
}
