package com.example.coupangclone.service.review;

import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.order.OrderItem;
import com.example.coupangclone.entity.review.Review;
import com.example.coupangclone.entity.review.command.ReviewUpdateCommand;
import com.example.coupangclone.entity.review.command.ReviewWriteCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.OrderStatus;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.order.OrderItemRepository;
import com.example.coupangclone.repository.review.ReviewRepository;
import com.example.coupangclone.result.ReviewResult;
import com.example.coupangclone.result.ReviewSummaryResult;
import com.example.coupangclone.util.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final List<OrderStatus> PURCHASED_STATUSES = List.of(
            OrderStatus.PAID, OrderStatus.SHIPPING, OrderStatus.DELIVERED
    );

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ReviewResult writeReview(ReviewWriteCommand command, User user) {
        OrderItem orderItem = orderItemRepository.findById(command.orderItemId())
                .orElseThrow(() -> new ErrorException(ExceptionEnum.ORDER_ITEM_NOT_FOUND));

        if (!orderItem.getOrder().getUser().getId().equals(user.getId())) {
            throw new ErrorException(ExceptionEnum.ORDER_ACCESS_DENIED);
        }
        if (!PURCHASED_STATUSES.contains(orderItem.getOrder().getStatus())) {
            throw new ErrorException(ExceptionEnum.REVIEW_NOT_PURCHASED);
        }
        if (reviewRepository.existsByOrderItemId(orderItem.getId())) {
            throw new ErrorException(ExceptionEnum.REVIEW_ALREADY_EXISTS);
        }

        // Item 행 잠금(벌크 UPDATE)을 리뷰 INSERT보다 먼저 획득한다. 순서를 반대로 하면
        // "리뷰 유니크 인덱스 잠금 대기 중 Item 잠금 보유" 트랜잭션과 "Item 잠금 대기 중
        // 유니크 인덱스 잠금 보유" 트랜잭션이 서로 맞물려 데드락(Deadlock found when trying
        // to get lock)이 발생할 수 있다(동시 리뷰 작성 테스트로 재현 확인).
        Item item = orderItem.getItem();
        itemRepository.applyReviewStatsDelta(item.getId(), 1, command.rating());

        Review review = Review.builder()
                .content(command.content())
                .rating(command.rating())
                .user(user)
                .item(item)
                .orderItem(orderItem)
                .build();
        reviewRepository.save(review);

        return ReviewMapper.toResult(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResult> getReviews(Long itemId, Pageable pageable) {
        return reviewRepository.findByItemId(itemId, pageable).map(ReviewMapper::toResult);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResult getSummary(Long itemId) {
        long reviewCount = reviewRepository.countByItemId(itemId);
        double averageRating = reviewCount == 0 ? 0.0 : reviewRepository.sumRatingByItemId(itemId) / reviewCount;

        return ReviewSummaryResult.builder()
                .itemId(itemId)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .build();
    }

    @Transactional
    public ReviewResult updateReview(Long reviewId, ReviewUpdateCommand command, User user) {
        Review review = getOwnedReview(reviewId, user);
        double previousRating = review.getRating();
        review.update(command.content(), command.rating());
        itemRepository.applyReviewStatsDelta(review.getItem().getId(), 0, command.rating() - previousRating);
        return ReviewMapper.toResult(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, User user) {
        Review review = getOwnedReview(reviewId, user);
        reviewRepository.delete(review);
        itemRepository.applyReviewStatsDelta(review.getItem().getId(), -1, -review.getRating());
    }

    private Review getOwnedReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ErrorException(ExceptionEnum.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ErrorException(ExceptionEnum.REVIEW_ACCESS_DENIED);
        }
        return review;
    }

}
