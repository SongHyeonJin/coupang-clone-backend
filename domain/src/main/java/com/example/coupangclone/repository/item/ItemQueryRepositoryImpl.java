package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.QBrand;
import com.example.coupangclone.entity.item.QCategory;
import com.example.coupangclone.entity.item.QItem;
import com.example.coupangclone.entity.item.QItemImage;
import com.example.coupangclone.entity.review.QReview;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class ItemQueryRepositoryImpl implements ItemQueryRepository {

    private static final QItem item = QItem.item;
    private static final QCategory category = QCategory.category;
    private static final QBrand brand = QBrand.brand;
    private static final QItemImage itemImage = QItemImage.itemImage;
    private static final QItemImage itemImageForMinId = new QItemImage("itemImageForMinId");
    private static final QReview review = QReview.review;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ItemListRow> findItemList(String keyword, Pageable pageable) {
        BooleanExpression keywordCondition = keywordCondition(keyword);

        List<Tuple> tuples = queryFactory
                .select(item, firstImageUrl(), avgRating(), reviewCount())
                .from(item)
                .leftJoin(item.category, category).fetchJoin()
                .leftJoin(item.brand, brand).fetchJoin()
                .where(keywordCondition)
                .orderBy(orderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<ItemListRow> content = tuples.stream()
                .map(tuple -> new ItemListRow(
                        tuple.get(item),
                        tuple.get(firstImageUrl()),
                        tuple.get(avgRating()),
                        tuple.get(reviewCount())
                ))
                .toList();

        return PageableExecutionUtils.getPage(content, pageable, () -> countItems(keywordCondition));
    }

    private long countItems(BooleanExpression keywordCondition) {
        Long count = queryFactory
                .select(item.count())
                .from(item)
                .leftJoin(item.brand, brand)
                .where(keywordCondition)
                .fetchOne();
        return count == null ? 0L : count;
    }

    private BooleanExpression keywordCondition(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return item.name.containsIgnoreCase(keyword)
                .or(brand.name.containsIgnoreCase(keyword));
    }

    // MySQL은 서브쿼리 내 LIMIT을 JPQL로 표현할 수 없어, item당 가장 작은
    // item_image_id(=최초 이미지)를 먼저 찾고 그 id로 이미지를 조회하는 2단 상관 서브쿼리로 대체.
    private com.querydsl.jpa.JPQLQuery<String> firstImageUrl() {
        return JPAExpressions
                .select(itemImage.image)
                .from(itemImage)
                .where(itemImage.item.eq(item), itemImage.id.eq(
                        JPAExpressions.select(itemImageForMinId.id.min())
                                .from(itemImageForMinId)
                                .where(itemImageForMinId.item.eq(item))
                ));
    }

    private com.querydsl.jpa.JPQLQuery<Double> avgRating() {
        return JPAExpressions
                .select(review.rating.avg())
                .from(review)
                .where(review.item.eq(item));
    }

    private com.querydsl.jpa.JPQLQuery<Long> reviewCount() {
        return JPAExpressions
                .select(review.count())
                .from(review)
                .where(review.item.eq(item));
    }

    private OrderSpecifier<?>[] orderSpecifiers(Sort sort) {
        return sort.stream()
                .map(this::toOrderSpecifier)
                .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;
        return "sale".equals(order.getProperty())
                ? new OrderSpecifier<>(direction, item.sale)
                : new OrderSpecifier<>(direction, item.createdAt);
    }

}
