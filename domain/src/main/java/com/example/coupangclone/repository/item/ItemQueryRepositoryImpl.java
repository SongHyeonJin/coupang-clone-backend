package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.QBrand;
import com.example.coupangclone.entity.item.QCategory;
import com.example.coupangclone.entity.item.QItem;
import com.example.coupangclone.entity.item.QItemImage;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
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

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ItemListRow> findItemList(String keyword, Pageable pageable) {
        BooleanExpression keywordCondition = keywordCondition(keyword);

        List<Tuple> tuples = queryFactory
                .select(item, firstImageUrl())
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
                        tuple.get(firstImageUrl())
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

    // item.ratingSum / item.reviewCount 로 평균을 구하되, 리뷰가 없는 상품(reviewCount=0)은
    // 0으로 처리한다. ratingSum 단순 정렬은 리뷰가 많은 상품이 유리해지므로 평균으로 정렬한다.
    private NumberExpression<Double> averageRatingExpression() {
        return new CaseBuilder()
                .when(item.reviewCount.eq(0)).then(0.0)
                .otherwise(item.ratingSum.divide(item.reviewCount));
    }

    private OrderSpecifier<?>[] orderSpecifiers(Sort sort) {
        return sort.stream()
                .map(this::toOrderSpecifier)
                .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;
        return switch (order.getProperty()) {
            case "sale" -> new OrderSpecifier<>(direction, item.sale);
            case "rating" -> new OrderSpecifier<>(direction, averageRatingExpression());
            default -> new OrderSpecifier<>(direction, item.createdAt);
        };
    }

}
