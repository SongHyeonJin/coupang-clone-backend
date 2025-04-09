package com.example.coupangclone.repository.review;

import com.example.coupangclone.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Long countByItemId(Long itemId);

    @Query("SELECT COALESCE(SUM(r.rating), 0.0) FROM Review r WHERE r.item.id = :itemId")
    double sumRatingByItemId(@Param("itemId") Long itemId);

}
