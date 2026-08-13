package com.example.coupangclone.toss;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(TossProperties.class)
public class TossConfig {

    @Bean
    public RestTemplate tossRestTemplate() {
        return new RestTemplate();
    }
}
