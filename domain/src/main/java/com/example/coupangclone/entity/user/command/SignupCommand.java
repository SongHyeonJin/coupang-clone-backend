package com.example.coupangclone.entity.user.command;

public record SignupCommand(
        String email,
        String password,
        String name,
        String tel,
        String gender
) {}
