package com.example.coupangclone.item.controller;

import com.example.coupangclone.auth.userdetails.UserDetailsImpl;
import com.example.coupangclone.item.dto.item.ItemRequestDto;
import com.example.coupangclone.item.dto.item.ItemResponseDto;
import com.example.coupangclone.item.service.ItemService;
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
public class ItemController {

    private final ItemService itemService;

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createItem(@RequestPart("data")ItemRequestDto requestDto,
                                        @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return itemService.createItem(requestDto, images, userDetails.getUser());
    }

    @GetMapping
    public ResponseEntity<Page<ItemResponseDto>> getItems(@RequestParam(name = "sort", defaultValue = "CREATED_AT") String sort,
                                                          @PageableDefault(size = 10) Pageable pageable,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return itemService.getItems(pageable, userDetails.getUser(), sort);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ItemResponseDto>> searchItems(@RequestParam("keyword") String keyword,
                                                             @PageableDefault(size = 10) Pageable pageable,
                                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return itemService.searchItems(keyword, pageable, userDetails.getUser());
    }

}
