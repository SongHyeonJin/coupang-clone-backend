package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByName(String name);
    Optional<Brand> findByName(String name);

}
