package com.example.coupangclone.item.controller;

import com.example.coupangclone.auth.userdetails.UserDetailsImpl;
import com.example.coupangclone.item.dto.item.ItemRequestDto;
import com.example.coupangclone.item.dto.item.ItemResponseDto;
import com.example.coupangclone.item.dto.item.SearchItemResponseDto;
import com.example.coupangclone.item.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
@Tag(name = "상품 API", description = "상품 생성 및 조회 API")
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "상품 등록", description = "상품 정보를 등록합니다. 이미지 파일은 Multipart 형식으로 첨부합니다.")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createItem(@Parameter(description = "상품 데이터(JSON)") @RequestPart("data") ItemRequestDto requestDto,
                                        @Parameter(description = "상품 이미지 파일들") @RequestPart(value = "images") List<MultipartFile> images,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return itemService.createItem(requestDto, images, userDetails.getUser());
    }

    @Operation(summary = "상품 전체 조회", description = "정렬 조건에 따라 전체 상품 목록을 페이징 조회합니다. (sort를 arg1 쪽 sort에 넣어야 제대로 동작. 예: 'createdAt', 'price_desc', 'price_asc')")
    @GetMapping
    public ResponseEntity<Page<ItemResponseDto>> getItems(@RequestParam(name = "sort", defaultValue = "createdAt") String sort,
                                                          @PageableDefault(size = 10) Pageable pageable,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return itemService.getItems(pageable, userDetails.getUser(), sort);
    }

    @Operation(summary = "상품 검색", description = "키워드로 상품을 검색합니다. (sort를 arg1 쪽 sort에 넣어야 제대로 동작. 예: 'createdAt', 'price_desc', 'price_asc')")
    @GetMapping("/search")
    public ResponseEntity<SearchItemResponseDto> searchItems(@RequestParam("keyword") String keyword,
                                                             @PageableDefault(size = 5) Pageable pageable,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return itemService.searchItems(keyword, pageable, userDetails.getUser());
    }

}
