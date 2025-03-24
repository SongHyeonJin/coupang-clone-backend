package com.example.coupangclone.user.enums;

public enum UserRoleEnum {
    USER(Authority.USER),
    ADMIN(Authority.ADMIN);

    UserRoleEnum(String authority) {
        this.authority = authority;
    }

    private final String authority;

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ADMIN";
    }
}
