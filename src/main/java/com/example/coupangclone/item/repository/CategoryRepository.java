package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

}
