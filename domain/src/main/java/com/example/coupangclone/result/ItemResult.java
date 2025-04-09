package com.example.coupangclone.result;

import lombok.Builder;

@Builder
public record ItemResult(
        String image,
        String name,
        String discountRate,
        int price,
        int sale,
        String unitPrice,
        String delivery,
        String deliveryPrice,
        double reviewRating,
        long reviewCnt,
        int point
) {}
