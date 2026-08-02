package com.yasarbilgi.visitormeetingmanagment.security.service.impl;

import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
        try {
            String key = buildKey(userId);
            String value = String.join(DELIMITER, permissions);

            redisTemplate.opsForValue().set(key, value, CACHE_TTL);
            log.debug("Cached {} permissions for user: {}", permissions.size(), userId);
        } catch (DataAccessException e) {
            log.warn(
                    "Redis'e izin cache'i yazılamadı (userId={}), izinler JWT üzerinden çalışmaya devam edecek: {}",
                    userId, e.getMessage()
            );
        }
    }

    @Override
    public Set<String> getCachedPermissions(Long userId) {
        try {
            String key = buildKey(userId);
            String value = redisTemplate.opsForValue().get(key);

            if (value == null || value.isBlank()) {
                return Set.of();
            }

            return Set.of(value.split(DELIMITER)).stream()
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());
        } catch (DataAccessException e) {
            log.warn(
                    "Redis'ten izin cache'i okunamadı (userId={}), JWT claim'lerine düşülecek: {}",
                    userId, e.getMessage()
            );
            return Set.of();
        }
    }

    @Override
    public void invalidate(Long userId) {
        try {
            String key = buildKey(userId);
            redisTemplate.delete(key);
            log.info("Invalidated permission cache for user: {}", userId);
        } catch (DataAccessException e) {
            log.warn(
                    "Redis cache invalidate edilemedi (userId={}); TTL dolana kadar (30 dk) eski izinler cache'de kalabilir: {}",
                    userId, e.getMessage()
            );
        }
    }

    @Override
    public boolean isCached(Long userId) {
        try {
            String key = buildKey(userId);
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (DataAccessException e) {
            log.warn("Redis'e erişilemedi, isCached kontrolü false dönüyor (userId={}): {}", userId, e.getMessage());
            return false;
        }
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}