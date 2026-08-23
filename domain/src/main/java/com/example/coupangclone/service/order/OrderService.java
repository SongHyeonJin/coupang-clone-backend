package com.example.coupangclone.service.order;

import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.order.Order;
import com.example.coupangclone.entity.order.OrderItem;
import com.example.coupangclone.entity.order.command.OrderCommand;
import com.example.coupangclone.entity.order.command.OrderItemCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.order.OrderRepository;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.result.OrderResult;
import com.example.coupangclone.service.payment.PaymentService;
import com.example.coupangclone.util.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    @Transactional
    public OrderResult createOrder(OrderCommand command, User user) {
        checkUser(user);

        if (command.items() == null || command.items().isEmpty()) {
            throw new ErrorException(ExceptionEnum.EMPTY_ORDER_ITEMS);
        }

        List<OrderItem> orderItems = command.items().stream()
                .sorted(Comparator.comparing(OrderItemCommand::itemId))
                .map(this::toOrderItem)
                .toList();

        Order order = Order.create(user, orderItems);
        orderRepository.save(order);

        return OrderMapper.toResult(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, User user) {
        Order order = getOwnedOrder(orderId, user);
        order.cancel();

        order.getOrderItems().stream()
                .sorted(Comparator.comparing(orderItem -> orderItem.getItem().getId()))
                .forEach(orderItem -> {
                    Item item = itemRepository.findByIdForUpdate(orderItem.getItem().getId())
                            .orElseThrow(() -> new ErrorException(ExceptionEnum.ITEM_NOT_FOUND));
                    item.increaseStock(orderItem.getQuantity());
                });

        // 외부 PG 호출은 순수 DB 검증/변경(주문취소, 재고복구)이 전부 성공한 뒤 마지막에 실행 —
        // 커밋 직전 실패로 "결제는 취소됐는데 DB엔 반영 안 됨" 남는 창을 최소화. 완전히 없애려면
        // 트랜잭션 밖으로 분리 + 아웃박스/재처리 필요 (실제 서비스로 키울 때 추가).
        paymentService.cancelForOrder(order);
    }

    @Transactional(readOnly = true)
    public OrderResult getOrder(Long orderId, User user) {
        return OrderMapper.toResult(getOwnedOrder(orderId, user));
    }

    @Transactional(readOnly = true)
    public Page<OrderResult> getOrders(Pageable pageable, User user) {
        checkUser(user);
        return orderRepository.findAllByUserId(user.getId(), pageable)
                .map(OrderMapper::toResult);
    }

    private OrderItem toOrderItem(OrderItemCommand itemCommand) {
        Item item = itemRepository.findByIdForUpdate(itemCommand.itemId())
                .orElseThrow(() -> new ErrorException(ExceptionEnum.ITEM_NOT_FOUND));
        item.decreaseStock(itemCommand.quantity());

        return OrderItem.builder()
                .quantity(itemCommand.quantity())
                .price(item.getSale())
                .item(item)
                .build();
    }

    private Order getOwnedOrder(Long orderId, User user) {
        checkUser(user);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ErrorException(ExceptionEnum.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ErrorException(ExceptionEnum.ORDER_ACCESS_DENIED);
        }
        return order;
    }

    private void checkUser(User user) {
        if (!userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ErrorException(ExceptionEnum.USER_NOT_FOUND);
        }
    }

}
