package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.Item;

public record ItemListRow(Item item, String imageUrl) {

    public double avgRatingOrZero() {
        return item.averageRating();
    }

    public long reviewCntOrZero() {
        return item.getReviewCount();
    }

}
