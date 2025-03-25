package com.example.coupangclone.item.service;

import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.item.dto.item.ItemRequestDto;
import com.example.coupangclone.item.entity.Brand;
import com.example.coupangclone.item.entity.Category;
import com.example.coupangclone.item.entity.Item;
import com.example.coupangclone.item.entity.ItemImage;
import com.example.coupangclone.item.repository.BrandRepository;
import com.example.coupangclone.item.repository.CategoryRepository;
import com.example.coupangclone.item.repository.ItemImageRepository;
import com.example.coupangclone.item.repository.ItemRepository;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private static final String IMAGE_DIR = "C:/Users/Song/Desktop/images/";
    private static final String IMAGE_URL_PREFIX = "/images/";

    @Transactional
    public ResponseEntity<?> createItem(ItemRequestDto requestDto,
                                        List<MultipartFile> images,
                                        User user) throws IOException {
        checkUser(user);

        Category category = checkCategory(requestDto);

        Brand brand = checkBrand(requestDto);

        Item item = Item.builder()
                .name(requestDto.getName())
                .content(requestDto.getContent())
                .price(requestDto.getPrice())
                .sale(requestDto.getSale())
                .stock(requestDto.getStock())
                .user(user)
                .category(category)
                .brand(brand)
                .build();
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

    private Brand checkBrand(ItemRequestDto requestDto) {
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
