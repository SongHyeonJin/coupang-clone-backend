package com.example.coupangclone.service.item;

import com.example.coupangclone.dto.BasicResponseDto;
import com.example.coupangclone.entity.item.Brand;
import com.example.coupangclone.entity.item.Category;
import com.example.coupangclone.entity.item.command.BrandCommand;
import com.example.coupangclone.entity.item.command.CategoryCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.item.BrandRepository;
import com.example.coupangclone.repository.item.CategoryRepository;
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

    private static void checkAdmin(User user) {
        if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new ErrorException(ExceptionEnum.NOT_ALLOW);
        }
    }

}
