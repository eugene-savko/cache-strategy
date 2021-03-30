package com.github.cache.strategy;

import com.github.cache.strategy.config.CacheStrategyConfig;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Import(CacheStrategyConfig.class)
public @interface EnableCacheStrategy {
}

