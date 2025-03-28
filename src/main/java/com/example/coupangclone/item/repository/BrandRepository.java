package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByName(String name);
    Optional<Brand> findByName(String name);

}
