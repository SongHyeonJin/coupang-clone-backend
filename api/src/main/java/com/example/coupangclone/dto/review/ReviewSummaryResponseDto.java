package com.example.coupangclone.dto.review;

import com.example.coupangclone.result.ReviewSummaryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "상품 리뷰 요약 응답 DTO")
public class ReviewSummaryResponseDto {

    private Long itemId;
    private double averageRating;
    private long reviewCount;

    public ReviewSummaryResponseDto(Long itemId, double averageRating, long reviewCount) {
        this.itemId = itemId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public static ReviewSummaryResponseDto from(ReviewSummaryResult result) {
        return new ReviewSummaryResponseDto(result.itemId(), result.averageRating(), result.reviewCount());
    }
}
