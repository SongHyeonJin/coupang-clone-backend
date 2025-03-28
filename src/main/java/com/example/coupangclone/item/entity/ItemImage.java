package com.example.coupangclone.item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_image_id")
    private Long id;

    @Lob
    @Column(nullable = false)
    private String image;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    public ItemImage(String image, Item item) {
        this.image = image;
        this.item = item;
    }

}
