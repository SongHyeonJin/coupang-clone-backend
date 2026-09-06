package com.example.coupangclone.entity.item;

import com.example.coupangclone.entity.base.Timestamped;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
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
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer deliveryTime;

    @Column(nullable = false)
    private Integer deliveryPrice;

    private Boolean isDeleted = false;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    // 평균이 아닌 총합을 저장한다. 평균만 저장하면 리뷰 수정/삭제 시 역산이 필요해 오차가 누적된다.
    @Column(nullable = false)
    private Double ratingSum = 0.0;

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
    public Item(String name, int weight, String content, int price, int sale, int saleCnt, int stockQuantity,
                int deliveryTime, int deliveryPrice, User user, Category category, Brand brand) {
        this.name = name;
        this.weight = weight;
        this.content = content;
        this.price = price;
        this.sale = sale;
        this.saleCnt = saleCnt;
        this.stockQuantity = stockQuantity;
        this.deliveryTime = deliveryTime;
        this.deliveryPrice = deliveryPrice;
        this.user = user;
        this.category = category;
        this.brand = brand;
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new ErrorException(ExceptionEnum.OUT_OF_STOCK);
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public double averageRating() {
        return reviewCount == 0 ? 0.0 : ratingSum / reviewCount;
    }

}
