package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query(value = """
            SELECT DISTINCT sk.keyword
            FROM search_keywords sk
            WHERE sk.keyword LIKE LOWER(CONCAT('%', :keyword, '%'))
            GROUP BY sk.keyword
            ORDER BY COUNT(sk.keyword) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<String> findTopByKeywordContaining(@Param("keyword") String keyword, @Param("limit") int limit);

}
