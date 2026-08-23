package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.Item;

public record ItemListRow(Item item, String imageUrl, Double avgRating, Long reviewCnt) {

    public double avgRatingOrZero() {
        return avgRating == null ? 0.0 : avgRating;
    }

    public long reviewCntOrZero() {
        return reviewCnt == null ? 0L : reviewCnt;
    }

}
