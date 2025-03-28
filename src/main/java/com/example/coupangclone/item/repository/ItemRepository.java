package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i LEFT JOIN i.brand b " +
            "WHERE i.name LIKE %:keyword% OR (b IS NOT NULL AND b.name LIKE %:keyword%)")
    Page<Item> searchByNameOrBrand(@Param("keyword") String keyword, Pageable pageable);

}
