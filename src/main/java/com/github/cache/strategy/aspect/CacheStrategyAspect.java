package com.github.cache.strategy.aspect;

import com.github.cache.strategy.CacheKey;
import com.github.cache.strategy.CacheStrategy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@RequiredArgsConstructor
public class CacheStrategyAspect {

    private final CacheManager cacheManager;

    @Around("@annotation(com.github.cache.strategy.CacheStrategy)")
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        CacheStrategy cacheAnnotation = method.getAnnotation(CacheStrategy.class);
        String cacheName = cacheAnnotation.cacheName();
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Cache manager doesn't contain cache with name: " + cacheName);
        }

        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
        Set<Object> keys = generateKeys(parameterAnnotations, joinPoint.getArgs());

        Map<Object, Object> cachedValues = getCachedValues(cache, keys);
        keys.removeAll(cachedValues.keySet());
        if (keys.isEmpty()) {
            log.debug("All values were found in cache '{}'", cacheName);
            return cachedValues;
        }

        log.debug("Absent keys '{}' in cache '{}' for cachedValues keys  '{}' for method '{}'", keys, cacheName, cachedValues.keySet(), method.getName());
        Object[] parameters = generateParameters(parameterAnnotations, joinPoint.getArgs(), keys);
        Object returnObject = joinPoint.proceed(parameters);
        saveValues(cache, returnObject);

        Map<Object, Object> newCachedValues = getCachedValues(cache, keys);
        cachedValues.putAll(newCachedValues);
        log.debug("Final return result '{}' for method '{}'", cachedValues, method.getName());
        return cachedValues;
    }

    private Map<Object, Object> getCachedValues(Cache cache, Set<Object> keys) {
        Map<Object, Object> cachedResult = new HashMap<>();
        for (Object key : keys) {
            Cache.ValueWrapper cachedValue = cache.get(key);
            if (cachedValue != null) {
                Object value = cachedValue.get();
                log.debug("Value '{}' has been found in cache for key '{}'", value, key);
                cachedResult.put(key, value);
            } else {
                log.debug("No actual value in cache '{}' for key '{}'", cache.getName(), key);
            }
        }
        return cachedResult;
    }

    private Set<Object> generateKeys(Annotation[][] parameterAnnotations, Object[] parameters) {
        Set<Object> keys = new HashSet<>();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            if (annotations.length == 0) {
                continue;
            }

            for (Annotation annotation : annotations) {
                if (!(annotation instanceof CacheKey)) {
                    continue;
                }

                if (parameters[i] instanceof Collection) {
                    keys.addAll(((Collection<Object>) parameters[i]));
                } else {
                    keys.add(parameters[i]);
                }
            }
        }

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("No parameter contains annotation CacheStrategy.CacheKey");
        }

        return keys;
    }

    private Object[] generateParameters(Annotation[][] parameterAnnotations, Object[] parameters, Set<Object> absentKeys) {
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            if (annotations.length == 0) {
                continue;
            }
            if (parameters[i] instanceof List) {
                parameters[i] = new ArrayList<>(absentKeys);
            } else if (parameters[i] instanceof Set) {
                parameters[i] = new HashSet<>(absentKeys);
            } else if (parameters[i] instanceof String) {
                parameters[i] = absentKeys.stream().findFirst().get();
            } else {
                throw new UnsupportedOperationException("Type " + parameters[i].getClass() + " not supported");
            }
        }
        return parameters;
    }

    private void saveValues(Cache cache, Object returnObject) {
        if (returnObject instanceof Map) {
            Map<Object, Object> result = (Map<Object, Object>) returnObject;
            result.forEach((key, value) -> {
                cache.put(key, value);
                log.debug("Value '{}' has been stored in cache for key '{}'", value, key);
            });
        } else {
            throw new UnsupportedOperationException("CacheStrategy supports only Map as method result");
        }
    }

}
