package com.example.coupangclone.util;

import com.example.coupangclone.entity.review.Review;
import com.example.coupangclone.result.ReviewResult;

public class ReviewMapper {

    public static ReviewResult toResult(Review review) {
        return ReviewResult.builder()
                .reviewId(review.getId())
                .itemId(review.getItem().getId())
                .userName(review.getUser().getName())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .build();
    }

}
