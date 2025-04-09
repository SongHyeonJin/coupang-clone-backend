package com.example.coupangclone.result;

import org.springframework.data.domain.Page;

import java.util.List;

public record SearchItemResult(Page<ItemResult> items, List<String> relatedKeywords) {
}
