package com.app.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class CacheConfig {

    @Value("${cache.base-currency:INR}")
    private String baseCurrency;

    @Value("${cache.ttl:120000}")
    private long cacheTtl;

    @Value("${cache.max-size:50}")
    private int maxCacheSize;
}
