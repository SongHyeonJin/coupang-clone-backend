package com.example.coupangclone.dto.item;

import com.example.coupangclone.result.ItemResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "전체 상품 응답 DTO")
public class ItemResponseDto {
    // 대표 이미지
    private String image;
    private String name;
    // 할인율
    private String discountRate;
    // 실제가격
    private int price;
    //판매가격
    private int sale;
    // 3개면 1개당 가격, 500g이면 100g당 가격
    private String unitPrice;
    // 예샹배달도착임
    private String delivery;
    // 예상배달가격 0원일 시 무료라고 찍혀야됨
    private String deliveryPrice;
    // 리뷰 점수 (총 점수/리뷰 수)
    private double reviewRating;
    // 리뷰 수
    private long reviewCnt;
    // 판매가의 1% 적립
    private int point;

    public ItemResponseDto(String image, String name, String discountRate, int price, int sale,
                           String unitPrice, String delivery, String deliveryPrice, double reviewRating,
                           long reviewCnt, int point) {
        this.image = image;
        this.name = name;
        this.discountRate = discountRate;
        this.price = price;
        this.sale = sale;
        this.unitPrice = unitPrice;
        this.delivery = delivery;
        this.deliveryPrice = deliveryPrice;
        this.reviewRating = reviewRating;
        this.reviewCnt = reviewCnt;
        this.point = point;
    }

    public static ItemResponseDto from(ItemResult result) {
        return new ItemResponseDto(
                result.image(),
                result.name(),
                result.discountRate(),
                result.price(),
                result.sale(),
                result.unitPrice(),
                result.delivery(),
                result.deliveryPrice(),
                result.reviewRating(),
                result.reviewCnt(),
                result.point()
        );
    }
}
