package com.example.coupangclone.entity.user.command;

public record LoginCommand(
        String email,
        String password
) {}
