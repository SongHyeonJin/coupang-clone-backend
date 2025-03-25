package com.example.coupangclone.item.controller;

import com.example.coupangclone.auth.userdetails.UserDetailsImpl;
import com.example.coupangclone.item.dto.category.CategoryRequestDto;
import com.example.coupangclone.item.service.AdminItemService;
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
public class AdminItemController {

    private final AdminItemService adminItemService;

    @PostMapping("/category")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequestDto requestDto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return adminItemService.createCategory(requestDto, userDetails.getUser());
    }

}
