package com.example.coupangclone.item.service;

import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.item.dto.brand.BrandRequestDto;
import com.example.coupangclone.item.dto.category.CategoryRequestDto;
import com.example.coupangclone.item.entity.Brand;
import com.example.coupangclone.item.entity.Category;
import com.example.coupangclone.item.repository.BrandRepository;
import com.example.coupangclone.item.repository.CategoryRepository;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminItemService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public ResponseEntity<?> createCategory(CategoryRequestDto requestDto, User user) {
        checkAdmin(user);

        if(categoryRepository.existsByName(requestDto.getName())) {
             throw new ErrorException(ExceptionEnum.CATEGORY_DUPLICATION);
        }

        Category parent = null;
        if (requestDto.getParent() != null) {
            parent = categoryRepository.findById(requestDto.getParent().getId()).orElseThrow(
                    () -> new ErrorException(ExceptionEnum.CATEGORY_NOT_FOUND)
            );
        }
        Category category = Category.builder()
                .name(requestDto.getName())
                .type(requestDto.getType())
                .parent(parent)
                .build();
        categoryRepository.save(category);
        return ResponseEntity.ok(BasicResponseDto.addSuccess("카테고리 등록이 완료되었습니다."));
    }

    @Transactional
    public ResponseEntity<?> createBrand(BrandRequestDto requestDto, User user) {
        checkAdmin(user);

        if (brandRepository.existsByName(requestDto.getName())) {
            throw new ErrorException(ExceptionEnum.BRAND_DUPLICATION);
        }

        Brand brand = Brand.builder()
                .name(requestDto.getName())
                .build();
        brandRepository.save(brand);
        return ResponseEntity.ok(BasicResponseDto.addSuccess("브랜드 등록이 완료되었습니다."));
    }

    private static void checkAdmin(User user) {
        if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new ErrorException(ExceptionEnum.PARENT_CATEGORY_NOT_FOUND);
        }
    }

}
