package com.example.coupangclone.user.controller;

import com.example.coupangclone.user.dto.SignupRequestDto;
import com.example.coupangclone.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/signup")
    public ResponseEntity<?> siggup(@Valid @RequestBody SignupRequestDto requestDto) {
        return userService.signup(requestDto);
    }

}
