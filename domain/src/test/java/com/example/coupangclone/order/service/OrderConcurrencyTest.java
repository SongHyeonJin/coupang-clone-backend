package com.example.coupangclone.order.service;

import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.order.command.OrderCommand;
import com.example.coupangclone.entity.order.command.OrderItemCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.order.OrderItemRepository;
import com.example.coupangclone.repository.order.OrderRepository;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.service.order.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class OrderConcurrencyTest {

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

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("재고 100개에 스레드 100개가 동시에 주문하면 정확히 100개만 성공하고 재고는 0이 된다.")
    @Test
    void concurrentOrders_exactlyExhaustStock() throws InterruptedException {
        // given
        int stockQuantity = 100;
        int threadCount = 100;

        User user = createUser("test@example.com");
        Item item = createItem("한정판 상품", 10000, 9000, stockQuantity);
        userRepository.save(user);
        itemRepository.save(item);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderCommand command = new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1)));
                    orderService.createOrder(command, user);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Item result = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(successCount.get()).isEqualTo(100);
        assertThat(failCount.get()).isEqualTo(0);
        assertThat(result.getStockQuantity()).isEqualTo(0);
        assertThat(orderRepository.findAll()).hasSize(100);
    }

    @DisplayName("재고보다 많은 스레드가 동시에 주문하면 재고만큼만 성공하고 나머지는 재고 부족으로 실패한다.")
    @Test
    void concurrentOrders_moreThreadsThanStock_onlyStockCountSucceeds() throws InterruptedException {
        // given
        int stockQuantity = 30;
        int threadCount = 100;

        User user = createUser("test@example.com");
        Item item = createItem("한정판 상품", 10000, 9000, stockQuantity);
        userRepository.save(user);
        itemRepository.save(item);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderCommand command = new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1)));
                    orderService.createOrder(command, user);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Item result = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(successCount.get()).isEqualTo(stockQuantity);
        assertThat(failCount.get()).isEqualTo(threadCount - stockQuantity);
        assertThat(result.getStockQuantity()).isEqualTo(0);
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
