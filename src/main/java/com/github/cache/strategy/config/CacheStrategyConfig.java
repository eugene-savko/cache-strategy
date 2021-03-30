package com.github.cache.strategy.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.cache.strategy.aspect.CacheStrategyAspect;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheStrategyConfig {

    @Bean
    public CacheStrategyAspect smartCacheAspect(@Value("${cache.strategy.names}") String[] cacheNames,
                                                @Value("${cache.strategy.timeToLiveSeconds}") long timeToLiveInSeconds) {
        return new CacheStrategyAspect(cacheStrategyManager(cacheNames, timeToLiveInSeconds));
    }

    public CacheManager cacheStrategyManager(String[] cacheNames, long timeToLiveInSeconds) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(cacheNames);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .expireAfterWrite(timeToLiveInSeconds, TimeUnit.SECONDS)
                .maximumSize(10000));
        return cacheManager;
    }

}
