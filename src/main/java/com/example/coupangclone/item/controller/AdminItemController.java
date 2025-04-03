package com.example.coupangclone.item.controller;

import com.example.coupangclone.auth.userdetails.UserDetailsImpl;
import com.example.coupangclone.item.dto.brand.BrandRequestDto;
import com.example.coupangclone.item.dto.category.CategoryRequestDto;
import com.example.coupangclone.item.service.AdminItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/item")
@Tag(name = "AdminItemController", description = "관리자 상품 관련 API")
public class AdminItemController {

    private final AdminItemService adminItemService;

    @Operation(summary = "카테고리 생성  (ADMIN 전용, type 예:THING, FOOD, LIQUID)", description = "관리자가 새로운 상품 카테고리를 생성합니다.")
    @PostMapping("/category")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequestDto requestDto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return adminItemService.createCategory(requestDto, userDetails.getUser());
    }

    @Operation(summary = "브랜드 생성  (ADMIN 전용)", description = "관리자가 새로운 상품 브랜드를 생성합니다.")
    @PostMapping("/brand")
    public ResponseEntity<?> createBrand(@RequestBody BrandRequestDto requestDto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return adminItemService.createBrand(requestDto, userDetails.getUser());
    }

}
