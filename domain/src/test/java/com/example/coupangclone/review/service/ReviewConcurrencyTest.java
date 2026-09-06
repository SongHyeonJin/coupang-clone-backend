package com.example.coupangclone.review.service;

import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.order.Order;
import com.example.coupangclone.entity.order.command.OrderCommand;
import com.example.coupangclone.entity.order.command.OrderItemCommand;
import com.example.coupangclone.entity.review.command.ReviewWriteCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.order.OrderItemRepository;
import com.example.coupangclone.repository.order.OrderRepository;
import com.example.coupangclone.repository.review.ReviewRepository;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.result.OrderResult;
import com.example.coupangclone.service.order.OrderService;
import com.example.coupangclone.service.review.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ActiveProfiles("test")
@SpringBootTest
class ReviewConcurrencyTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAllInBatch();
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("같은 상품에 스레드 50개가 동시에 리뷰를 작성해도 reviewCount/ratingSum이 정확히 반영된다.")
    @Test
    void concurrentReviews_exactCountAndSum() throws InterruptedException {
        // given
        int threadCount = 50;
        Item item = createItem("노트북", 1200000, 1060000, threadCount);
        itemRepository.save(item);

        List<User> users = new ArrayList<>();
        List<Long> orderItemIds = new ArrayList<>();
        List<Double> ratings = new ArrayList<>();
        double expectedRatingSum = 0.0;
        for (int i = 0; i < threadCount; i++) {
            User user = createUser("buyer" + i + "@example.com");
            userRepository.save(user);
            users.add(user);
            orderItemIds.add(orderAndPay(user, item));

            double rating = (i % 5) + 1;
            ratings.add(rating);
            expectedRatingSum += rating;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            User user = users.get(i);
            Long orderItemId = orderItemIds.get(i);
            double rating = ratings.get(i);
            executorService.submit(() -> {
                try {
                    ReviewWriteCommand command = ReviewWriteCommand.builder()
                            .orderItemId(orderItemId)
                            .content("동시성 테스트 리뷰")
                            .rating(rating)
                            .build();
                    reviewService.writeReview(command, user);
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
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);
        assertThat(reviewRepository.count()).isEqualTo(threadCount);
        assertThat(result.getReviewCount()).isEqualTo(threadCount);
        assertThat(result.getRatingSum()).isCloseTo(expectedRatingSum, within(0.001));
    }

    private Long orderAndPay(User user, Item item) {
        OrderResult order = orderService.createOrder(
                new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1))), user);
        Order savedOrder = orderRepository.findById(order.orderId()).orElseThrow();
        savedOrder.markAsPaid();
        orderRepository.save(savedOrder);
        return orderItemRepository.findByOrder_Id(order.orderId()).get(0).getId();
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
