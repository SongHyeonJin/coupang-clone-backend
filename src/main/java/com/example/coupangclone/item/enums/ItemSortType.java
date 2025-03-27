package com.example.coupangclone.item.enums;

import org.springframework.data.domain.Sort;

public enum ItemSortType {
    CREATED_AT_DESC("createdAt", Sort.Direction.DESC),
    PRICE_ASC("sale", Sort.Direction.ASC),
    PRICE_DESC("sale", Sort.Direction.DESC);

    private final String property;
    private final Sort.Direction direction;

    public String getProperty() {
        return property;
    }

    public Sort.Direction getDirection() {
        return direction;
    }

    ItemSortType(String property, Sort.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public static ItemSortType from(String value) {
        try {
            return ItemSortType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CREATED_AT_DESC;
        }
    }
}
