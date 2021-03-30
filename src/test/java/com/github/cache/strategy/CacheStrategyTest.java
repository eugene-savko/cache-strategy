package com.github.cache.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = CacheStrategyTest.TestCacheConfig.class)
public class CacheStrategyTest {

    @Configuration
    @EnableCacheStrategy
    @EnableAspectJAutoProxy
    @PropertySource("classpath:application.properties")
    public static class TestCacheConfig {
        @Bean
        public CacheableService smartCacheService() {
            return new CacheableService();
        }
    }

    @Service
    public static class CacheableService {

        @CacheStrategy(cacheName = "incorrectName")
        public Collection<String> incorrectCacheName(String entityId, @CacheKey List<String> userIds) {
            return Collections.emptyList();
        }

        @CacheStrategy(cacheName = "default")
        public Collection<String> parametersWithoutAnnotation(String entityId, List<String> userIds) {
            return Collections.emptyList();
        }

        @CacheStrategy(cacheName = "default")
        public Map<String, Long> getUserUids(String entityId, @CacheKey List<String> userIds) {
            return userIds.stream().collect(Collectors.toMap(Function.identity(), userId -> System.nanoTime()));
        }

    }

    @Autowired
    private CacheableService cacheableService;

    @Test
    void cacheNameTest() {
        Executable executable = () -> cacheableService.incorrectCacheName("test-uid", Collections.emptyList());
        assertThrows(IllegalStateException.class, executable);
    }

    @Test
    void cacheKeyTest() {
        Executable executable = () -> cacheableService.parametersWithoutAnnotation("test-uid", Collections.emptyList());
        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    void simpleCacheTest() {
        List<String> userIds1 = Arrays.asList("test1", "test2", "test3");
        Map<String, Long> result1 = cacheableService.getUserUids("test-uid", userIds1);
        assertEquals(3, result1.size());

        List<String> userIds2 = Arrays.asList("test3", "test4", "test1");
        Map<String, Long> result2 = cacheableService.getUserUids("test-uid", userIds2);
        assertEquals(3, result2.size());

        assertEquals(result1.get("test1"), result2.get("test1"));
        assertEquals(result1.get("test3"), result2.get("test3"));
    }

}
