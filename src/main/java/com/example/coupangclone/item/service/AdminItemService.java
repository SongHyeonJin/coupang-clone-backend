package com.example.coupangclone.item.service;

import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.item.dto.category.CategoryRequestDto;
import com.example.coupangclone.item.entity.Category;
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

    @Transactional
    public ResponseEntity<?> createCategory(CategoryRequestDto requestDto, User user) {
        checkAdmin(user);

        Category parent = null;
        if (requestDto.getParent() != null) {
            parent = categoryRepository.findById(requestDto.getParent().getId()).orElseThrow(
                    () -> new ErrorException(ExceptionEnum.CATEGORY_NOT_FOUND)
            );
        }
        Category category = Category.builder()
                .name(requestDto.getName())
                .parent(parent)
                .build();
        categoryRepository.save(category);
        return ResponseEntity.ok(BasicResponseDto.addSuccess("카테고리 등록이 완료되었습니다."));
    }

    private static void checkAdmin(User user) {
        if (!user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new ErrorException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
    }

}
