package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

}
