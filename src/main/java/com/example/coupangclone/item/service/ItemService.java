package com.example.coupangclone.item.service;

import com.example.coupangclone.config.S3Uploader;
import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.item.dto.item.ItemRequestDto;
import com.example.coupangclone.item.dto.item.ItemResponseDto;
import com.example.coupangclone.item.dto.item.SearchItemResponseDto;
import com.example.coupangclone.item.entity.*;
import com.example.coupangclone.item.enums.ItemSortType;
import com.example.coupangclone.item.repository.*;
import com.example.coupangclone.review.repository.ReviewRepository;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ReviewRepository reviewRepository;
    private final SearchLogRepository searchLogRepository;
    private final SearchLogService searchLogService;
    private final S3Uploader s3Uploader;

    @Transactional
    public ResponseEntity<?> createItem(ItemRequestDto requestDto,
                                        List<MultipartFile> images,
                                        User user) throws IOException {
        checkUser(user);

        Category category = checkCategory(requestDto);

        Brand brand = checkBrand(requestDto);

        Item item = addItem(requestDto, user, category, brand);
        itemRepository.save(item);

        List<ItemImage> itemImages = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = s3Uploader.upload(image);

                ItemImage itemImage = ItemImage.builder()
                        .image(imageUrl)
                        .item(item)
                        .build();
                itemImages.add(itemImage);
            }
        }
        itemImageRepository.saveAll(itemImages);

        return ResponseEntity.ok(BasicResponseDto.addSuccess("상품 등록이 완료되었습니다."));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<ItemResponseDto>> getItems(Pageable pageable, User user, String sort) {
        checkUser(user);

        ItemSortType sortType = ItemSortType.from(sort);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(sortType.getDirection(), sortType.getProperty())
        );

        Page<Item> itemPage = itemRepository.findAll(sortedPageable);

        Page<ItemResponseDto> responseDto = itemPage.map(item -> {
            String imageUrl = itemImageRepository.findFirstByItemId(item.getId())
                    .map(ItemImage::getImage)
                    .orElse(null);

            double reviewRating = reviewRepository.sumRatingByItemId(item.getId());
            long reviewCnt = reviewRepository.countByItemId(item.getId());
            double reviewAvgRating = reviewCnt == 0 ? 0.0 : reviewRating / reviewCnt;

            return ItemResponseDto.of(item, imageUrl, reviewAvgRating, reviewCnt);
        });

        return ResponseEntity.ok(responseDto);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<SearchItemResponseDto> searchItems(String keyword, Pageable pageable, User user) {
        checkUser(user);

        if (StringUtils.hasText(keyword)) {
            searchLogService.saveKeyword(keyword);
        }

        List<String> relatedKeywords = searchLogService.getRelatedKeywordsFor(keyword);

        Page<Item> itemPage = itemRepository.searchByNameOrBrand(keyword, pageable);

        Page<ItemResponseDto> responseDto = itemPage.map(item -> {
            String imageUrl = itemImageRepository.findFirstByItemId(item.getId())
                    .map(ItemImage::getImage)
                    .orElse(null);

            double reviewRating = reviewRepository.sumRatingByItemId(item.getId());
            long reviewCnt = reviewRepository.countByItemId(item.getId());
            double reviewAvgRating = reviewCnt == 0 ? 0.0 : reviewRating / reviewCnt;

            return ItemResponseDto.of(item, imageUrl, reviewAvgRating, reviewCnt);
        });

        return ResponseEntity.ok(SearchItemResponseDto.builder()
                .items(responseDto)
                .relatedKeywords(relatedKeywords)
                .build()
        );
    }

    private static Item addItem(ItemRequestDto requestDto, User user, Category category, Brand brand) {
        return Item.builder()
                .name(requestDto.getName())
                .weight(requestDto.getWeight())
                .content(requestDto.getContent())
                .price(requestDto.getPrice())
                .sale(requestDto.getSale())
                .saleCnt(requestDto.getSaleCnt())
                .deliveryTime(requestDto.getDeliveryTime())
                .deliveryPrice(requestDto.getDeliveryPrice())
                .user(user)
                .category(category)
                .brand(brand)
                .build();
    }

    private Brand checkBrand(ItemRequestDto requestDto) {
        if (requestDto.getBrandId() == null) return null;

        Brand brand = brandRepository.findById(requestDto.getBrandId()).orElseThrow(
                () -> new ErrorException(ExceptionEnum.BRAND_NOT_FOUND)
        );
        return brand;
    }

    private Category checkCategory(ItemRequestDto requestDto) {
        Category category = categoryRepository.findById(requestDto.getCategoryId()).orElseThrow(
                () -> new ErrorException(ExceptionEnum.CATEGORY_NOT_FOUND)
        );
        return category;
    }

    private void checkUser(User user) {
        userRepository.findByEmail(user.getEmail()).orElseThrow(
                () -> new ErrorException(ExceptionEnum.USER_NOT_FOUND)
        );
    }

}
