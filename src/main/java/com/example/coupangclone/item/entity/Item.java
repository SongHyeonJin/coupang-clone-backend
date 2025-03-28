package com.example.coupangclone.item.entity;

import com.example.coupangclone.global.common.Timestamped;
import com.example.coupangclone.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private int weight;

    @Lob
    private String content;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer sale;

    @Column(nullable = false)
    private Integer saleCnt;

    @Column(nullable = false)
    private Integer deliveryTime;

    @Column(nullable = false)
    private Integer deliveryPrice;

    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Builder
    public Item(String name, int weight, String content, int price, int sale, int saleCnt,
                int deliveryTime, int deliveryPrice, User user, Category category, Brand brand) {
        this.name = name;
        this.weight = weight;
        this.content = content;
        this.price = price;
        this.sale = sale;
        this.saleCnt = saleCnt;
        this.deliveryTime = deliveryTime;
        this.deliveryPrice = deliveryPrice;
        this.user = user;
        this.category = category;
        this.brand = brand;
    }

}
