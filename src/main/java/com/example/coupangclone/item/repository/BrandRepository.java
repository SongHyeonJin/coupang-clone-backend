package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByName(String name);

}
