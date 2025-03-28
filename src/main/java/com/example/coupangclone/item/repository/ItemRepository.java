package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Brand;
import com.example.coupangclone.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i LEFT JOIN i.brand b " +
            "WHERE i.name LIKE %:keyword% OR (b IS NOT NULL AND b.name LIKE %:keyword%)")
    Page<Item> searchByNameOrBrand(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM Item i LEFT JOIN i.brand b " +
            "WHERE LOWER(i.name) LIKE LOWER(%:keyword%) " +
            "OR (b IS NOT NULL AND LOWER(b.name) LIKE LOWER(%:keyword%))")
    List<Item> findMatchingItemsByNameOrBrand(@Param("keyword") String keyword);

    List<Item> findTop5ByBrand(Brand brand);

}
