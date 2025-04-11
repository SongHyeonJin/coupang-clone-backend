package com.example.coupangclone.service.item;

import com.example.coupangclone.auth.S3UploadPort;
import com.example.coupangclone.entity.item.command.ItemCommand;
import com.example.coupangclone.repository.item.*;
import com.example.coupangclone.repository.review.ReviewRepository;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.dto.BasicResponseDto;
import com.example.coupangclone.entity.item.Brand;
import com.example.coupangclone.entity.item.Category;
import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.item.ItemImage;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.ItemSortType;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.result.ItemResult;
import com.example.coupangclone.result.SearchItemResult;
import com.example.coupangclone.util.ItemMapper;
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
    private final S3UploadPort s3Uploader;

    @Transactional
    public void createItem(ItemCommand command,
                           List<MultipartFile> images,
                           User user) throws IOException {
        checkUser(user);

        Category category = checkCategory(command);

        Brand brand = checkBrand(command);

        Item item = addItem(command, user, category, brand);
        itemRepository.save(item);

        if (images==null || images.isEmpty()) {
            throw new ErrorException(ExceptionEnum.IMAGE_REQUIRED);
        }

        List<ItemImage> itemImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item)
                    .build();
            itemImages.add(itemImage);
        }

        itemImageRepository.saveAll(itemImages);
    }

    @Transactional(readOnly = true)
    public Page<ItemResult> getItems(Pageable pageable, User user, String sort) {
        checkUser(user);

        ItemSortType sortType = ItemSortType.from(sort);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(sortType.getDirection(), sortType.getProperty())
        );

        Page<Item> itemPage = itemRepository.findAll(sortedPageable);

        return itemPage.map(item -> {
            String imageUrl = itemImageRepository.findFirstByItemId(item.getId())
                    .map(ItemImage::getImage)
                    .orElse(null);

            double reviewRating = reviewRepository.sumRatingByItemId(item.getId());
            long reviewCnt = reviewRepository.countByItemId(item.getId());
            double reviewAvgRating = reviewCnt == 0 ? 0.0 : reviewRating / reviewCnt;

            return ItemMapper.toResult(item, imageUrl, reviewAvgRating, reviewCnt);
        });
    }

    @Transactional(readOnly = true)
    public SearchItemResult searchItems(String keyword, Pageable pageable, User user) {
        checkUser(user);

        if (StringUtils.hasText(keyword)) {
            searchLogService.saveKeyword(keyword);
        }

        List<String> relatedKeywords = searchLogService.getRelatedKeywordsFor(keyword);

        Page<Item> itemPage = itemRepository.searchByNameOrBrand(keyword, pageable);

        Page<ItemResult> itemResults = itemPage.map(item -> {
            String imageUrl = itemImageRepository.findFirstByItemId(item.getId())
                    .map(ItemImage::getImage)
                    .orElse(null);

            double reviewRating = reviewRepository.sumRatingByItemId(item.getId());
            long reviewCnt = reviewRepository.countByItemId(item.getId());
            double reviewAvgRating = reviewCnt == 0 ? 0.0 : reviewRating / reviewCnt;

            return ItemMapper.toResult(item, imageUrl, reviewAvgRating, reviewCnt);
        });

        return  new SearchItemResult(itemResults, relatedKeywords);
    }

    private static Item addItem(ItemCommand command, User user, Category category, Brand brand) {
        return Item.builder()
                .name(command.name())
                .weight(command.weight())
                .content(command.content())
                .price(command.price())
                .sale(command.sale())
                .saleCnt(command.saleCnt())
                .deliveryTime(command.deliveryTime())
                .deliveryPrice(command.deliveryPrice())
                .user(user)
                .category(category)
                .brand(brand)
                .build();
    }

    private Brand checkBrand(ItemCommand command) {
        if (command.brandId() == null) return null;

        Brand brand = brandRepository.findById(command.brandId()).orElseThrow(
                () -> new ErrorException(ExceptionEnum.BRAND_NOT_FOUND)
        );
        return brand;
    }

    private Category checkCategory(ItemCommand command) {
        Category category = categoryRepository.findById(command.categoryId()).orElseThrow(
                () -> new ErrorException(ExceptionEnum.CATEGORY_NOT_FOUND)
        );
        return category;
    }

    private void checkUser(User user) {
        if (!userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ErrorException(ExceptionEnum.USER_NOT_FOUND);
        }
    }

}
