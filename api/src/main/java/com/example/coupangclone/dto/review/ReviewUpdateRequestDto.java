package com.example.coupangclone.dto.review;

import com.example.coupangclone.entity.review.command.ReviewUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "리뷰 수정 요청 DTO")
public class ReviewUpdateRequestDto {

    @Schema(description = "리뷰 내용", example = "다시 써보니 더 좋아요.")
    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String content;

    @Schema(description = "평점 (1.0~5.0)", example = "4.0")
    @NotNull(message = "평점을 입력해주세요.")
    @DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    private Double rating;

    public ReviewUpdateCommand toCommand() {
        return ReviewUpdateCommand.builder()
                .content(content)
                .rating(rating)
                .build();
    }
}
