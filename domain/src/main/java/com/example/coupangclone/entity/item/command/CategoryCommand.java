package com.example.coupangclone.entity.item.command;

import com.example.coupangclone.enums.ItemTypeEnum;
import lombok.Builder;

@Builder
public record CategoryCommand(
        String name,
        ItemTypeEnum type,
        Long parentId
) {}
