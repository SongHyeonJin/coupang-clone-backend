package com.example.coupangclone.dto.review;

import com.example.coupangclone.entity.review.command.ReviewWriteCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "리뷰 작성 요청 DTO")
public class ReviewWriteRequestDto {

    @Schema(description = "주문 상품 ID (주문 조회 응답의 orderItemId)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "주문 상품 ID를 입력해주세요.")
    private Long orderItemId;

    @Schema(description = "리뷰 내용", example = "만족스러운 상품이에요.")
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String content;

    @Schema(description = "평점 (1.0~5.0)", example = "5.0")
    @NotNull(message = "평점을 입력해주세요.")
    @DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    private Double rating;

    public ReviewWriteCommand toCommand() {
        return ReviewWriteCommand.builder()
                .orderItemId(orderItemId)
                .content(content)
                .rating(rating)
                .build();
    }
}
