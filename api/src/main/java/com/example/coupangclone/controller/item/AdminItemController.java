package com.example.coupangclone.controller.item;

import com.example.coupangclone.dto.BasicResponseDto;
import com.example.coupangclone.dto.brand.BrandRequestDto;
import com.example.coupangclone.dto.category.CategoryRequestDto;
import com.example.coupangclone.result.CategoryResult;
import com.example.coupangclone.service.item.AdminItemService;
import com.example.coupangclone.security.userdetails.UserDetailsImpl;
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
        CategoryResult result = adminItemService.createCategory(requestDto.toCommand(), userDetails.getUser());
        return ResponseEntity.ok(BasicResponseDto.addSuccess("카테고리 등록 완료"));
    }

    @Operation(summary = "브랜드 생성  (ADMIN 전용)", description = "관리자가 새로운 상품 브랜드를 생성합니다.")
    @PostMapping("/brand")
    public ResponseEntity<?> createBrand(@RequestBody BrandRequestDto requestDto,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        adminItemService.createBrand(requestDto.toCommand(), userDetails.getUser());
        return ResponseEntity.ok(BasicResponseDto.addSuccess("브랜드 등록이 완료되었습니다."));
    }

}
