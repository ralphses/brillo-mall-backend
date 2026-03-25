package com.clickstechnology.Brillo.Mall.infrastructure.caching;

import com.clickstechnology.Brillo.Mall.application.exception.ApplicationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void set(String key, Object value, long ttlSeconds) {
        try {
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            log.error("Failed to set cache for key={}", key, e);
            throw new ApplicationException("Cache set operation failed", e);
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            if (ttl != null) {
                redisTemplate.opsForValue().set(key, value, ttl);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            log.error("Failed to set cache for key={}", key, e);
            throw new ApplicationException("Cache set operation failed", e);
        }
    }

    public void set(String key, Object value) {
        set(key, value, (Duration) null);
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                return null;
            }

            // Already correct type
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }

            // Convert LinkedHashMap → DTO
            return objectMapper.convertValue(value, clazz);

        } catch (Exception e) {
            log.error("Failed to get cache for key={}", key, e);
            throw new ApplicationException("Cache get operation failed", e);
        }
    }

    public boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Failed to check key existence for key={}", key, e);
            return false;
        }
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to evict key={}", key, e);
        }
    }

    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Failed to increment key={}", key, e);
            throw new ApplicationException("Cache increment failed", e);
        }
    }

    public void expire(String key, long seconds) {
        try {
            redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to set TTL for key={}", key, e);
        }
    }

    public Long getTtl(String key) {
        try {
            return redisTemplate.getExpire(key);
        } catch (Exception e) {
            log.error("Failed to get TTL for key={}", key, e);
            return null;
        }
    }

    public void pushToList(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("Failed to push to list key={}", key, e);
            throw new ApplicationException("Cache list push failed", e);
        }
    }

    public List<Object> popBatch(String key, int size) {
        List<Object> list = new ArrayList<>();

        try {
            for (int i = 0; i < size; i++) {
                Object value = redisTemplate.opsForList().leftPop(key);
                if (value == null) break;
                list.add(value);
            }
        } catch (Exception e) {
            log.error("Failed to pop batch from key={}", key, e);
            throw new ApplicationException("Cache batch pop failed", e);
        }

        return list;
    }
}