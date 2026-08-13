package com.example.coupangclone.service.payment;

import com.example.coupangclone.auth.PaymentGatewayPort;
import com.example.coupangclone.entity.order.Order;
import com.example.coupangclone.entity.payment.Payment;
import com.example.coupangclone.entity.payment.command.PaymentConfirmCommand;
import com.example.coupangclone.enums.PaymentStatus;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.order.OrderRepository;
import com.example.coupangclone.repository.payment.PaymentRepository;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.result.PaymentResult;
import com.example.coupangclone.util.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;

    @Transactional
    public PaymentResult confirm(PaymentConfirmCommand command, User user) {
        Order order = getOwnedOrder(command.orderId(), user);

        if (!order.getTotalPrice().equals(command.amount())) {
            throw new ErrorException(ExceptionEnum.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> Payment.builder().order(order).amount(command.amount()).build());

        String method = paymentGatewayPort.confirm(command.paymentKey(), tossOrderId(order.getId()), command.amount());

        payment.markDone(command.paymentKey(), method);
        order.markAsPaid();
        paymentRepository.save(payment);

        return PaymentMapper.toResult(payment);
    }

    @Transactional
    public void cancelForOrder(Order order) {
        paymentRepository.findByOrderId(order.getId())
                .filter(payment -> payment.getStatus() == PaymentStatus.DONE)
                .ifPresent(payment -> {
                    paymentGatewayPort.cancel(payment.getPaymentKey(), "주문 취소");
                    payment.markCanceled();
                });
    }

    private static String tossOrderId(Long orderId) {
        return "ORDER-" + orderId;
    }

    private Order getOwnedOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ErrorException(ExceptionEnum.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ErrorException(ExceptionEnum.ORDER_ACCESS_DENIED);
        }
        return order;
    }

}
