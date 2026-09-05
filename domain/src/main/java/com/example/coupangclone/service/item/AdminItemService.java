package com.example.coupangclone.service.item;

import com.example.coupangclone.dto.BasicResponseDto;
import com.example.coupangclone.entity.item.Brand;
import com.example.coupangclone.entity.item.Category;
import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.entity.item.command.BrandCommand;
import com.example.coupangclone.entity.item.command.CategoryCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.item.BrandRepository;
import com.example.coupangclone.repository.item.CategoryRepository;
import com.example.coupangclone.repository.item.ItemRepository;
import com.example.coupangclone.repository.review.ReviewRepository;
import com.example.coupangclone.result.CategoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminItemService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ItemRepository itemRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public CategoryResult createCategory(CategoryCommand command, User user) {
        checkAdmin(user);

        if(categoryRepository.existsByName(command.name())) {
             throw new ErrorException(ExceptionEnum.CATEGORY_DUPLICATION);
        }

        Category parent = null;
        if (command.parentId() != null) {
            parent = categoryRepository.findById(command.parentId()).orElseThrow(
                    () -> new ErrorException(ExceptionEnum.CATEGORY_NOT_FOUND)
            );
        }
        Category category = Category.builder()
                .name(command.name())
                .type(command.type())
                .parent(parent)
                .build();
        categoryRepository.save(category);
        return new CategoryResult(category.getName());
    }

    @Transactional
    public void createBrand(BrandCommand command, User user) {
        checkAdmin(user);

        if (brandRepository.existsByName(command.name())) {
            throw new ErrorException(ExceptionEnum.BRAND_DUPLICATION);
        }

        Brand brand = Brand.builder()
                .name(command.name())
                .build();
        brandRepository.save(brand);
    }

    // 상품 평점/리뷰 수 반정규화(Item.reviewCount/ratingSum) 도입 전에 쌓인 리뷰 데이터를
    // 기준으로 값을 채우는 일회성 백필. 실제 리뷰 데이터로 절대값을 SET하므로 몇 번을 다시
    // 실행해도 결과가 같다(멱등) -- 운영 배포 직후 관리자가 한 번 호출하면 된다.
    @Transactional
    public void backfillReviewStats(User user) {
        checkAdmin(user);

        for (Item item : itemRepository.findAll()) {
            long reviewCount = reviewRepository.countByItemId(item.getId());
            double ratingSum = reviewRepository.sumRatingByItemId(item.getId());
            itemRepository.setReviewStats(item.getId(), (int) reviewCount, ratingSum);
        }
    }

    private static void checkAdmin(User user) {
        if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new ErrorException(ExceptionEnum.NOT_ALLOW);
        }
    }

}
