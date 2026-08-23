package com.example.coupangclone.repository.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemQueryRepository {

    Page<ItemListRow> findItemList(String keyword, Pageable pageable);

}
