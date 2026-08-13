package com.example.coupangclone.payment.service;

import com.example.coupangclone.auth.PaymentGatewayPort;
import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.order.command.OrderCommand;
import com.example.coupangclone.entity.order.command.OrderItemCommand;
import com.example.coupangclone.entity.payment.command.PaymentConfirmCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.OrderStatus;
import com.example.coupangclone.enums.PaymentStatus;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.payment.support.FakePaymentGateway;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.order.OrderItemRepository;
import com.example.coupangclone.repository.order.OrderRepository;
import com.example.coupangclone.repository.payment.PaymentRepository;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.result.OrderResult;
import com.example.coupangclone.result.PaymentResult;
import com.example.coupangclone.service.order.OrderService;
import com.example.coupangclone.service.payment.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Import(PaymentServiceTest.TestPaymentGatewayConfig.class)
class PaymentServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAllInBatch();
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("결제 승인에 성공하면 결제와 주문이 완료 상태가 된다.")
    @Test
    void confirm_success() {
        // given
        User user = createUser("test@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);

        OrderResult order = orderService.createOrder(
                new OrderCommand(java.util.List.of(new OrderItemCommand(item.getId(), 1))), user);
        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_PENDING.name());

        PaymentConfirmCommand command = PaymentConfirmCommand.builder()
                .orderId(order.orderId())
                .paymentKey("test-payment-key")
                .amount(order.totalPrice())
                .build();

        // when
        PaymentResult result = paymentService.confirm(command, user);

        // then
        assertThat(result.status()).isEqualTo(PaymentStatus.DONE.name());
        assertThat(result.method()).isEqualTo("카드");

        OrderResult paidOrder = orderService.getOrder(order.orderId(), user);
        assertThat(paidOrder.status()).isEqualTo(OrderStatus.PAID.name());
    }

    @DisplayName("결제 금액이 주문 금액과 다르면 결제 승인에 실패한다.")
    @Test
    void confirm_fail_amount_mismatch() {
        // given
        User user = createUser("test@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);

        OrderResult order = orderService.createOrder(
                new OrderCommand(java.util.List.of(new OrderItemCommand(item.getId(), 1))), user);

        PaymentConfirmCommand command = PaymentConfirmCommand.builder()
                .orderId(order.orderId())
                .paymentKey("test-payment-key")
                .amount(order.totalPrice() - 1)
                .build();

        // when // then
        assertThatThrownBy(() -> paymentService.confirm(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.PAYMENT_AMOUNT_MISMATCH.getMsg());
    }

    @DisplayName("결제완료 후 주문을 취소하면 결제도 취소된다.")
    @Test
    void cancelOrder_cancelsPayment() {
        // given
        User user = createUser("test@example.com");
        Item item = createItem("노트북", 1200000, 1060000, 10);
        userRepository.save(user);
        itemRepository.save(item);

        OrderResult order = orderService.createOrder(
                new OrderCommand(java.util.List.of(new OrderItemCommand(item.getId(), 1))), user);

        paymentService.confirm(PaymentConfirmCommand.builder()
                .orderId(order.orderId())
                .paymentKey("test-payment-key")
                .amount(order.totalPrice())
                .build(), user);

        // when
        orderService.cancelOrder(order.orderId(), user);

        // then
        assertThat(paymentRepository.findByOrderId(order.orderId()).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.CANCELED);

        Item savedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(savedItem.getStockQuantity()).isEqualTo(10);
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

    @TestConfiguration
    static class TestPaymentGatewayConfig {
        @Bean
        @Primary
        public PaymentGatewayPort paymentGatewayPort() {
            return new FakePaymentGateway();
        }
    }

}
