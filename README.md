## cache-strategy: A library for applying the strategy of caching the computed values of a method using aspect annotation.

## Getting Started

## Examples
```java
@CacheStrategy(cacheName = "cachename2")
public Map<String, Long> getUserUids(String entityId, @CacheKey List<String> userIds) {
    return userIds.stream().collect(Collectors.toMap(Function.identity(), userId -> System.nanoTime()));
}
```
