package com.example.coupangclone.entity.review;

import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.base.Timestamped;
import com.example.coupangclone.entity.order.OrderItem;
import com.example.coupangclone.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Lob
    private String content;

    private Double rating;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @OneToOne
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    @Builder
    public Review(String content, Double rating, User user, Item item, OrderItem orderItem) {
        this.content = content;
        this.rating = rating;
        this.user = user;
        this.item = item;
        this.orderItem = orderItem;
    }

    public void update(String content, Double rating) {
        this.content = content;
        this.rating = rating;
    }

}
