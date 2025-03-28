package com.example.coupangclone.item.enums;

public enum ItemTypeEnum {
    FOOD("음식"),
    LIQUID("액체"),
    THING("제품");

    private final String type;

    ItemTypeEnum(String type) {
        this.type = type;
    }
}
