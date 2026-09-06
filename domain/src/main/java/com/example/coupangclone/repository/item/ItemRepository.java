package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.Brand;
import com.example.coupangclone.entity.item.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemQueryRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Optional<Item> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT i FROM Item i
            LEFT JOIN i.brand b
            WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR (b IS NOT NULL AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Item> searchByNameOrBrand(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT i FROM Item i LEFT JOIN i.brand b
            WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR (b IS NOT NULL AND LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Item> findMatchingItemsByNameOrBrand(@Param("keyword") String keyword);

    List<Item> findTop5ByBrand(Brand brand);

    // 리뷰 작성/수정/삭제 시 read-modify-write 없이 원자적으로 반영하기 위한 벌크 UPDATE.
    // - flushAutomatically: 같은 트랜잭션에서 대기 중인 리뷰 저장/삭제가 있다면 이 벌크
    //   쿼리 실행 전에 먼저 flush한다. 그렇지 않으면 뒤이은 clearAutomatically(컨텍스트 초기화)가
    //   Hibernate Session.clear()의 "대기 중인 저장/수정/삭제 취소" 동작 때문에 그 리뷰
    //   저장/삭제 자체를 통째로 날려버린다.
    // - clearAutomatically: 벌크 연산은 영속성 컨텍스트를 거치지 않으므로, 같은 트랜잭션에서
    //   이미 로드된 Item이 갱신 후 flush되며 이 값을 덮어쓰지 않도록 컨텍스트를 비운다.
    // reviewCount는 GREATEST로 0 미만이 되지 않도록 방어한다.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Item i
            SET i.reviewCount = FUNCTION('GREATEST', 0, i.reviewCount + :countDelta),
                i.ratingSum = i.ratingSum + :ratingSumDelta
            WHERE i.id = :itemId
            """)
    void applyReviewStatsDelta(@Param("itemId") Long itemId,
                                @Param("countDelta") int countDelta,
                                @Param("ratingSumDelta") double ratingSumDelta);

    // 기존 리뷰 데이터를 기준으로 reviewCount/ratingSum을 절대값으로 재설정하는 백필용 메서드.
    // 델타가 아닌 절대값 SET이라 몇 번을 재실행해도 결과가 같다(멱등).
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Item i SET i.reviewCount = :reviewCount, i.ratingSum = :ratingSum WHERE i.id = :itemId")
    void setReviewStats(@Param("itemId") Long itemId,
                         @Param("reviewCount") int reviewCount,
                         @Param("ratingSum") double ratingSum);

}
