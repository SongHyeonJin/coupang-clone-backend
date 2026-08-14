package com.example.coupangclone.review.service;

import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.order.Order;
import com.example.coupangclone.entity.order.command.OrderCommand;
import com.example.coupangclone.entity.order.command.OrderItemCommand;
import com.example.coupangclone.entity.review.command.ReviewUpdateCommand;
import com.example.coupangclone.entity.review.command.ReviewWriteCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.order.OrderItemRepository;
import com.example.coupangclone.repository.order.OrderRepository;
import com.example.coupangclone.repository.review.ReviewRepository;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.result.OrderResult;
import com.example.coupangclone.result.ReviewResult;
import com.example.coupangclone.result.ReviewSummaryResult;
import com.example.coupangclone.service.order.OrderService;
import com.example.coupangclone.service.review.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@ActiveProfiles("test")
@SpringBootTest
class ReviewServiceTest {

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

    @DisplayName("결제완료된 주문 상품에는 리뷰를 작성할 수 있다.")
    @Test
    void writeReview_success() {
        // given
        User user = createUser("buyer@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);
        Long orderItemId = orderAndPay(user, item);

        ReviewWriteCommand command = ReviewWriteCommand.builder()
                .orderItemId(orderItemId)
                .content("만족스러운 상품이에요.")
                .rating(5.0)
                .build();

        // when
        ReviewResult result = reviewService.writeReview(command, user);

        // then
        assertThat(result.content()).isEqualTo("만족스러운 상품이에요.");
        assertThat(result.rating()).isEqualTo(5.0);
        assertThat(result.itemId()).isEqualTo(item.getId());
        assertThat(reviewRepository.count()).isEqualTo(1);
    }

    @DisplayName("결제완료 전(PAYMENT_PENDING) 주문 상품에는 리뷰를 작성할 수 없다.")
    @Test
    void writeReview_fail_notPurchasedYet() {
        // given
        User user = createUser("buyer@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);

        OrderResult order = orderService.createOrder(
                new OrderCommand(List.of(new OrderItemCommand(item.getId(), 1))), user);
        Long orderItemId = orderItemRepository.findByOrder_Id(order.orderId()).get(0).getId();

        ReviewWriteCommand command = ReviewWriteCommand.builder()
                .orderItemId(orderItemId)
                .content("아직 결제 전인데 씀")
                .rating(5.0)
                .build();

        // when // then
        assertThatThrownBy(() -> reviewService.writeReview(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.REVIEW_NOT_PURCHASED.getMsg());
    }

    @DisplayName("본인이 주문하지 않은 주문 상품에는 리뷰를 작성할 수 없다.")
    @Test
    void writeReview_fail_notOwner() {
        // given
        User owner = createUser("owner@example.com");
        User other = createUser("other@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(owner);
        userRepository.save(other);
        itemRepository.save(item);
        Long orderItemId = orderAndPay(owner, item);

        ReviewWriteCommand command = ReviewWriteCommand.builder()
                .orderItemId(orderItemId)
                .content("남의 주문에 리뷰")
                .rating(5.0)
                .build();

        // when // then
        assertThatThrownBy(() -> reviewService.writeReview(command, other))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.ORDER_ACCESS_DENIED.getMsg());
    }

    @DisplayName("같은 주문 상품에는 리뷰를 두 번 작성할 수 없다.")
    @Test
    void writeReview_fail_alreadyExists() {
        // given
        User user = createUser("buyer@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);
        Long orderItemId = orderAndPay(user, item);

        ReviewWriteCommand command = ReviewWriteCommand.builder()
                .orderItemId(orderItemId)
                .content("첫 리뷰")
                .rating(4.0)
                .build();
        reviewService.writeReview(command, user);

        // when // then
        assertThatThrownBy(() -> reviewService.writeReview(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.REVIEW_ALREADY_EXISTS.getMsg());
    }

    @DisplayName("같은 상품을 재구매하면 새 주문 상품 건으로 리뷰를 추가 작성할 수 있다.")
    @Test
    void writeReview_success_afterRepurchase() {
        // given
        User user = createUser("buyer@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);

        Long firstOrderItemId = orderAndPay(user, item);
        reviewService.writeReview(ReviewWriteCommand.builder()
                .orderItemId(firstOrderItemId).content("처음엔 별로였어요").rating(2.0).build(), user);

        Long secondOrderItemId = orderAndPay(user, item);

        // when
        ReviewResult secondReview = reviewService.writeReview(ReviewWriteCommand.builder()
                .orderItemId(secondOrderItemId).content("재구매했는데 이번엔 좋아요").rating(5.0).build(), user);

        // then
        assertThat(secondReview.content()).isEqualTo("재구매했는데 이번엔 좋아요");
        assertThat(reviewRepository.count()).isEqualTo(2);
    }

    @DisplayName("본인이 작성한 리뷰를 수정할 수 있다.")
    @Test
    void updateReview_success() {
        // given
        User user = createUser("buyer@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);
        Long orderItemId = orderAndPay(user, item);

        ReviewResult review = reviewService.writeReview(ReviewWriteCommand.builder()
                .orderItemId(orderItemId).content("처음 리뷰").rating(3.0).build(), user);

        // when
        ReviewResult updated = reviewService.updateReview(review.reviewId(),
                ReviewUpdateCommand.builder().content("다시 써보니 더 좋아요").rating(5.0).build(), user);

        // then
        assertThat(updated.content()).isEqualTo("다시 써보니 더 좋아요");
        assertThat(updated.rating()).isEqualTo(5.0);
        assertThat(reviewRepository.count()).isEqualTo(1);
    }

    @DisplayName("다른 사람이 작성한 리뷰는 수정할 수 없다.")
    @Test
    void updateReview_fail_notOwner() {
        // given
        User owner = createUser("owner@example.com");
        User other = createUser("other@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(owner);
        userRepository.save(other);
        itemRepository.save(item);
        Long orderItemId = orderAndPay(owner, item);

        ReviewResult review = reviewService.writeReview(ReviewWriteCommand.builder()
                .orderItemId(orderItemId).content("리뷰").rating(3.0).build(), owner);

        // when // then
        assertThatThrownBy(() -> reviewService.updateReview(review.reviewId(),
                ReviewUpdateCommand.builder().content("수정 시도").rating(1.0).build(), other))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.REVIEW_ACCESS_DENIED.getMsg());
    }

    @DisplayName("본인이 작성한 리뷰를 삭제할 수 있다.")
    @Test
    void deleteReview_success() {
        // given
        User user = createUser("buyer@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);
        Long orderItemId = orderAndPay(user, item);

        ReviewResult review = reviewService.writeReview(ReviewWriteCommand.builder()
                .orderItemId(orderItemId).content("리뷰").rating(3.0).build(), user);

        // when
        reviewService.deleteReview(review.reviewId(), user);

        // then
        assertThat(reviewRepository.count()).isEqualTo(0);
    }

    @DisplayName("다른 사람이 작성한 리뷰는 삭제할 수 없다.")
    @Test
    void deleteReview_fail_notOwner() {
        // given
        User owner = createUser("owner@example.com");
        User other = createUser("other@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(owner);
        userRepository.save(other);
        itemRepository.save(item);
        Long orderItemId = orderAndPay(owner, item);

        ReviewResult review = reviewService.writeReview(ReviewWriteCommand.builder()
                .orderItemId(orderItemId).content("리뷰").rating(3.0).build(), owner);

        // when // then
        assertThatThrownBy(() -> reviewService.deleteReview(review.reviewId(), other))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.REVIEW_ACCESS_DENIED.getMsg());

        assertThat(reviewRepository.count()).isEqualTo(1);
    }

    @DisplayName("상품 리뷰 요약은 평균 평점과 리뷰 개수를 계산한다.")
    @Test
    void getSummary_calculatesAverage() {
        // given
        User buyerA = createUser("buyerA@example.com");
        User buyerB = createUser("buyerB@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(buyerA);
        userRepository.save(buyerB);
        itemRepository.save(item);
        Long orderItemA = orderAndPay(buyerA, item);
        Long orderItemB = orderAndPay(buyerB, item);

        reviewService.writeReview(ReviewWriteCommand.builder().orderItemId(orderItemA).content("좋아요").rating(4.0).build(), buyerA);
        reviewService.writeReview(ReviewWriteCommand.builder().orderItemId(orderItemB).content("최고예요").rating(5.0).build(), buyerB);

        // when
        ReviewSummaryResult summary = reviewService.getSummary(item.getId());

        // then
        assertThat(summary.reviewCount()).isEqualTo(2);
        assertThat(summary.averageRating()).isCloseTo(4.5, within(0.001));
        assertThat(reviewService.getReviews(item.getId(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(2);
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
