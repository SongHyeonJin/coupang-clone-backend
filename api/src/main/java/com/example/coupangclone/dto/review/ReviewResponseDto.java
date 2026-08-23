package com.example.coupangclone.dto.review;

import com.example.coupangclone.result.ReviewResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "리뷰 응답 DTO")
public class ReviewResponseDto {

    private Long reviewId;
    private Long itemId;
    private String userName;
    private String content;
    private Double rating;
    private LocalDateTime createdAt;

    public ReviewResponseDto(Long reviewId, Long itemId, String userName, String content,
                              Double rating, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.itemId = itemId;
        this.userName = userName;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public static ReviewResponseDto from(ReviewResult result) {
        return new ReviewResponseDto(
                result.reviewId(),
                result.itemId(),
                result.userName(),
                result.content(),
                result.rating(),
                result.createdAt()
        );
    }
}
