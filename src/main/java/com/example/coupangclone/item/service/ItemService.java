package com.example.coupangclone.item.service;

import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.item.dto.item.ItemRequestDto;
import com.example.coupangclone.item.dto.item.ItemResponseDto;
import com.example.coupangclone.item.entity.Brand;
import com.example.coupangclone.item.entity.Category;
import com.example.coupangclone.item.entity.Item;
import com.example.coupangclone.item.entity.ItemImage;
import com.example.coupangclone.item.enums.ItemTypeEnum;
import com.example.coupangclone.item.repository.BrandRepository;
import com.example.coupangclone.item.repository.CategoryRepository;
import com.example.coupangclone.item.repository.ItemImageRepository;
import com.example.coupangclone.item.repository.ItemRepository;
import com.example.coupangclone.review.repository.ReviewRepository;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ReviewRepository reviewRepository;
    private static final String IMAGE_DIR = "C:/Users/Song/Desktop/images/";
    private static final String IMAGE_URL_PREFIX = "/images/";

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
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                File targetFile = new File(IMAGE_DIR, fileName);

                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }

                image.transferTo(targetFile);
                String imageUrl = IMAGE_URL_PREFIX + fileName;

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
    public ResponseEntity<Page<ItemResponseDto>> getItems(Pageable pageable, User user) {
        checkUser(user);

        Page<Item> itemPage = itemRepository.findAll(pageable);

        Page<ItemResponseDto> responseDto = itemPage.map(item -> {
            String imageUrl = itemImageRepository.findFirstByItemId(item.getId())
                    .map(ItemImage::getImage)
                    .orElse(null);

            double reviewRating = reviewRepository.sumRatingByItemId(item.getId());
            long reviewCnt = reviewRepository.countByItemId(item.getId());
            double reviewAvgRating = reviewRating / reviewCnt;

            return ItemResponseDto.of(item, imageUrl, reviewAvgRating, reviewCnt);
        });

        return ResponseEntity.ok(responseDto);
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
