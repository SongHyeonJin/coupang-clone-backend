package com.example.coupangclone.context;

public class TokenHolder {

    private TokenHolder() {}

    private static final ThreadLocal<String> accessToken = new ThreadLocal<>();
    private static final ThreadLocal<String> refreshToken = new ThreadLocal<>();

    public static void set(String access, String refresh) {
        accessToken.set(access);
        refreshToken.set(refresh);
    }

    public static String getAccessToken() { return accessToken.get(); }
    public static String getRefreshToken() { return refreshToken.get(); }

    public static void clear() {
        accessToken.remove();
        refreshToken.remove();
    }
}