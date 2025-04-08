package com.example.coupangclone.auth;

import java.util.concurrent.TimeUnit;

public interface RedisPort {
    void set(String key, String value, long duration, TimeUnit unit);
    String get(String key);
    void delete(String key);
    boolean hasKey(String key);
}
