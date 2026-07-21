package com.yasarbilgi.visitormeetingmanagment.security.service.impl;

import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Efektif kullanıcı izinlerini Redis'te cache'ler. Her API isteğinde
 * izinleri veritabanından yeniden hesaplamak yerine, burada saklanan
 * değeri okuyarak performans kazanılır. Rol/izin değişikliğinde
 * invalidate() çağrılarak cache anında geçersiz kılınır.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private static final String KEY_PREFIX = "permissions:user:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);
    private static final String DELIMITER = ",";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void cachePermissions(Long userId, Set<String> permissions) {
        String key = buildKey(userId);
        String value = String.join(DELIMITER, permissions);

        redisTemplate.opsForValue().set(key, value, CACHE_TTL);
        log.debug("Cached {} permissions for user: {}", permissions.size(), userId);
    }

    @Override
    public Set<String> getCachedPermissions(Long userId) {
        String key = buildKey(userId);
        String value = redisTemplate.opsForValue().get(key);

        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Set.of(value.split(DELIMITER)).stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    public void invalidate(Long userId) {
        String key = buildKey(userId);
        redisTemplate.delete(key);
        log.info("Invalidated permission cache for user: {}", userId);
    }

    @Override
    public boolean isCached(Long userId) {
        String key = buildKey(userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}