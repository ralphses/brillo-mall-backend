package com.clickstechnology.Brillo.Mall.infrastructure.caching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /* Set cache with optional TTL */
    public void set(String key, Object value, long ttlSeconds) {
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /* Overloaded – no TTL */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /* Get cached value */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : clazz.cast(value);
    }

    /* Check if key exists */
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /* Delete key */
    public void evict(String key) {
        redisTemplate.delete(key);
    }

    /* Increment a numeric cache key */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /* Set TTL */
    public void expire(String key, long seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /* Get TTL */
    public Long getTtl(String key) {
        return redisTemplate.getExpire(key);
    }
}
