package com.github.cache.strategy;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCacheStrategy
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new CaffeineCacheManager();
    }

}

