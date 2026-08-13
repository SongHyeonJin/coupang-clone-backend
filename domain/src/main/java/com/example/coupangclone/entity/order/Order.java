package com.example.coupangclone.entity.order;

import com.example.coupangclone.entity.base.Timestamped;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.OrderStatus;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false)
    private Integer totalPrice = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    private Order(User user) {
        this.user = user;
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public static Order create(User user, List<OrderItem> orderItems) {
        Order order = new Order(user);
        orderItems.forEach(order::addOrderItem);
        return order;
    }

    private void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.assignOrder(this);
        this.totalPrice += orderItem.getPrice() * orderItem.getQuantity();
    }

    public void markAsPaid() {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new ErrorException(ExceptionEnum.PAYMENT_NOT_ALLOWED);
        }
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELED) {
            throw new ErrorException(ExceptionEnum.ORDER_ALREADY_CANCELED);
        }
        if (this.status == OrderStatus.SHIPPING || this.status == OrderStatus.DELIVERED) {
            throw new ErrorException(ExceptionEnum.ORDER_CANCEL_NOT_ALLOWED);
        }
        this.status = OrderStatus.CANCELED;
    }

}
