package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
