# cache-strategy

# Getting Started

#Example 1
```java
@CacheStrategy(cacheName = "cachename1")
public Collection<String> parametersWithoutAnnotation(String entityId, List<String> userIds) {
    return Collections.emptyList();
}
```

#Example 2
```java
@CacheStrategy(cacheName = "cachename2")
public Map<String, Long> getUserUids(String entityId, @CacheKey List<String> userIds) {
    return userIds.stream().collect(Collectors.toMap(Function.identity(), userId -> System.nanoTime()));
}
```
