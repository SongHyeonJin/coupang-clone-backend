package com.example.coupangclone.repository.order;

import com.example.coupangclone.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);

}
