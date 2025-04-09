package com.example.coupangclone.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {

    @Value("${thread.pool.core-size}")
    private int corePoolSize;

    @Value("${thread.pool.max-size}")
    private int maxPoolSize;

    @Value("${thread.pool.queue-capacity}")
    private int queueCapacity;

    private final Environment environment;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("async-exec-");
        executor.initialize();
        return executor;
    }

    @PostConstruct
    public void logThreadPoolSetting() {
        String activeProfile = Arrays.toString(environment.getActiveProfiles());
        log.info("✅ [ENV 확인용] activeProfile={}, core={}, max={}, queue={}",
                activeProfile, corePoolSize, maxPoolSize, queueCapacity);
    }

}
