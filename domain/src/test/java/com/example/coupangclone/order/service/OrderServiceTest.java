package com.example.coupangclone.order.service;

import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.order.command.OrderCommand;
import com.example.coupangclone.entity.order.command.OrderItemCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.OrderStatus;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.order.OrderItemRepository;
import com.example.coupangclone.repository.order.OrderRepository;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.result.OrderResult;
import com.example.coupangclone.service.order.OrderService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("주문 생성에 성공하면 재고가 차감된다.")
    @Test
    void createOrder_success() {
        // given
        User user = createUser("test@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);

        OrderCommand command = new OrderCommand(List.of(new OrderItemCommand(item.getId(), 3)));

        // when
        OrderResult result = orderService.createOrder(command, user);

        // then
        assertThat(result.status()).isEqualTo(OrderStatus.PAYMENT_PENDING.name());
        assertThat(result.totalPrice()).isEqualTo(1060000 * 3);
        assertThat(result.items()).hasSize(1);

        Item savedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(savedItem.getStockQuantity()).isEqualTo(7);
    }

    @DisplayName("여러 상품 중 하나라도 재고가 부족하면 주문 전체가 실패하고 롤백된다.")
    @Test
    void createOrder_fail_out_of_stock_rollback() {
        // given
        User user = createUser("test@example.com");
        Item item1 = createItem("노트북", 1200000, 1060000, 10);
        Item item2 = createItem("마우스", 30000, 25000, 2);
        userRepository.save(user);
        itemRepository.save(item1);
        itemRepository.save(item2);

        OrderCommand command = new OrderCommand(List.of(
                new OrderItemCommand(item1.getId(), 3),
                new OrderItemCommand(item2.getId(), 5)
        ));

        // when // then
        assertThatThrownBy(() -> orderService.createOrder(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.OUT_OF_STOCK.getMsg());

        Item savedItem1 = itemRepository.findById(item1.getId()).orElseThrow();
        assertThat(savedItem1.getStockQuantity()).isEqualTo(10);
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @DisplayName("존재하지 않는 상품으로 주문하면 실패한다.")
    @Test
    void createOrder_fail_item_not_found() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);

        OrderCommand command = new OrderCommand(List.of(new OrderItemCommand(999L, 1)));

        // when // then
        assertThatThrownBy(() -> orderService.createOrder(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.ITEM_NOT_FOUND.getMsg());
    }

    @DisplayName("주문 상품이 비어있으면 실패한다.")
    @Test
    void createOrder_fail_empty_items() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);

        OrderCommand command = new OrderCommand(List.of());

        // when // then
        assertThatThrownBy(() -> orderService.createOrder(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.EMPTY_ORDER_ITEMS.getMsg());
    }

    @DisplayName("회원이 아니면 주문 생성에 실패한다.")
    @Test
    void createOrder_fail_not_found_user() {
        // given
        User user = createUser("test@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        itemRepository.save(item);

        OrderCommand command = new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1)));

        // when // then
        assertThatThrownBy(() -> orderService.createOrder(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.USER_NOT_FOUND.getMsg());
    }

    @DisplayName("주문 취소에 성공하면 재고가 복구되고 상태가 취소로 바뀐다.")
    @Test
    void cancelOrder_success() {
        // given
        User user = createUser("test@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);
        OrderResult created = orderService.createOrder(
                new OrderCommand(List.of(new OrderItemCommand(item.getId(), 3))), user);

        // when
        orderService.cancelOrder(created.orderId(), user);

        // then
        OrderResult canceled = orderService.getOrder(created.orderId(), user);
        assertThat(canceled.status()).isEqualTo(OrderStatus.CANCELED.name());

        Item savedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(savedItem.getStockQuantity()).isEqualTo(10);
    }

    @DisplayName("이미 취소된 주문을 다시 취소하면 실패한다.")
    @Test
    void cancelOrder_fail_already_canceled() {
        // given
        User user = createUser("test@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);
        OrderResult created = orderService.createOrder(
                new OrderCommand(List.of(new OrderItemCommand(item.getId(), 3))), user);
        orderService.cancelOrder(created.orderId(), user);

        // when // then
        assertThatThrownBy(() -> orderService.cancelOrder(created.orderId(), user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.ORDER_ALREADY_CANCELED.getMsg());
    }

    @DisplayName("본인의 주문이 아니면 조회 및 취소에 실패한다.")
    @Test
    void order_fail_access_denied_for_other_user() {
        // given
        User owner = createUser("owner@example.com");
        User other = createUser("other@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(owner);
        userRepository.save(other);
        itemRepository.save(item);
        OrderResult created = orderService.createOrder(
                new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1))), owner);

        // when // then
        assertThatThrownBy(() -> orderService.getOrder(created.orderId(), other))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.ORDER_ACCESS_DENIED.getMsg());

        assertThatThrownBy(() -> orderService.cancelOrder(created.orderId(), other))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.ORDER_ACCESS_DENIED.getMsg());
    }

    @DisplayName("존재하지 않는 주문을 조회하면 실패한다.")
    @Test
    void getOrder_fail_not_found() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);

        // when // then
        assertThatThrownBy(() -> orderService.getOrder(999L, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.ORDER_NOT_FOUND.getMsg());
    }

    @DisplayName("본인의 주문 목록을 페이징 조회한다.")
    @Test
    void getOrders_success() {
        // given
        User user = createUser("test@example.com");
        User other = createUser("other@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        userRepository.save(other);
        itemRepository.save(item);

        orderService.createOrder(new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1))), user);
        orderService.createOrder(new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1))), user);
        orderService.createOrder(new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1))), other);

        // when
        Page<OrderResult> result = orderService.getOrders(PageRequest.of(0, 10), user);

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @DisplayName("주문 목록 조회 시 주문 건수가 늘어나도 쿼리 수는 고정된다 (N+1 회귀 테스트).")
    @Test
    void getOrders_query_count_is_fixed_regardless_of_order_count() {
        // given
        User user = createUser("test@example.com");
        userRepository.save(user);
        Item item1 = createItem("노트북", 1200000, 1060000, 1000);
        Item item2 = createItem("마우스", 30000, 25000, 1000);
        itemRepository.save(item1);
        itemRepository.save(item2);

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        createOrdersWithTwoItems(user, item1, item2, 3);
        statistics.clear();

        // when
        // 페이지 크기(20)를 두 케이스의 주문 건수보다 항상 크게 잡아, Spring Data JPA의
        // count 쿼리 생략 최적화(content.size() < pageSize)가 두 측정에서 동일하게 적용되도록 한다.
        orderService.getOrders(PageRequest.of(0, 20), user);
        long statementCountForThreeOrders = statistics.getPrepareStatementCount();

        createOrdersWithTwoItems(user, item1, item2, 7);
        statistics.clear();

        orderService.getOrders(PageRequest.of(0, 20), user);
        long statementCountForTenOrders = statistics.getPrepareStatementCount();

        // then
        assertThat(statementCountForTenOrders).isEqualTo(statementCountForThreeOrders);
    }

    private void createOrdersWithTwoItems(User user, Item item1, Item item2, int count) {
        for (int i = 0; i < count; i++) {
            orderService.createOrder(new OrderCommand(List.of(
                    new OrderItemCommand(item1.getId(), 1),
                    new OrderItemCommand(item2.getId(), 1)
            )), user);
        }
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .password("qwer123!")
                .name("김서방")
                .tel("01043215678")
                .gender("남성")
                .role(UserRoleEnum.USER)
                .build();
    }

    private Item createItem(String name, int price, int sale, int stockQuantity) {
        return Item.builder()
                .name(name)
                .content("설명")
                .price(price)
                .sale(sale)
                .saleCnt(1)
                .stockQuantity(stockQuantity)
                .deliveryTime(1)
                .deliveryPrice(0)
                .build();
    }

}
