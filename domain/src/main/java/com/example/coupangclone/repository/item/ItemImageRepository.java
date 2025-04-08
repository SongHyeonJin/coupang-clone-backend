package com.example.coupangclone.repository.item;

import com.example.coupangclone.entity.item.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    Optional<ItemImage> findFirstByItemId(Long itemId);

}
