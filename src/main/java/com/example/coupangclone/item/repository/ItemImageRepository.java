package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    Optional<ItemImage> findFirstByItemId(Long itemId);

}
