package com.example.coupangclone.service.item;

import com.example.coupangclone.entity.item.Brand;
import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.item.SearchLog;
import com.example.coupangclone.repository.item.BrandRepository;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.item.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final BrandRepository brandRepository;
    private final ItemRepository itemRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) return;

        Optional<Brand> brandOpt = brandRepository.findByName(keyword);

        if (brandOpt.isPresent()) {
            Brand brand = brandOpt.get();

            List<Item> brandItems = itemRepository.findTop5ByBrand(brand);
            if (brandItems.isEmpty()) return;

            List<String> itemNames = brandItems.stream()
                    .map(Item::getName)
                    .distinct()
                    .limit(5)
                    .toList();

            SearchLog log = SearchLog.builder()
                    .keywords(itemNames)
                    .mainKeyword(brand.getName())
                    .brand(brand.getName())
                    .build();
            searchLogRepository.save(log);
            return;
        }

        List<Item> matchingItems = itemRepository.findMatchingItemsByNameOrBrand(keyword);
        if (matchingItems.isEmpty()) return;

        Brand existsBrand = matchingItems.stream()
                .filter(item -> item.getBrand() != null)
                .collect(Collectors.groupingBy(Item::getBrand, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String brandName = existsBrand != null ? existsBrand.getName() : null;

        SearchLog log = SearchLog.builder()
                .keywords(List.of(keyword))
                .mainKeyword(keyword)
                .brand(brandName)
                .build();

        searchLogRepository.save(log);
    }


    @Transactional(readOnly = true)
    public List<String> getRelatedKeywordsFor(String keyword) {
        if (!StringUtils.hasText(keyword)) return Collections.emptyList();

        Optional<Brand> brandOpt = brandRepository.findByName(keyword);

        if (brandOpt.isPresent()) {
            Brand brand = brandOpt.get();
            List<Item> brandItems = itemRepository.findTop5ByBrand(brand);
            if (brandItems.isEmpty()) return List.of();

            return brandItems.stream()
                    .map(Item::getName)
                    .flatMap(itemName ->
                            searchLogRepository
                                    .findTopByKeywordContaining(itemName, 3)
                                    .stream())
                    .filter(k -> !k.equalsIgnoreCase(keyword))
                    .distinct()
                    .limit(5)
                    .toList();
        }
        return searchLogRepository.findTopByKeywordContaining(keyword, 5);
    }
}
