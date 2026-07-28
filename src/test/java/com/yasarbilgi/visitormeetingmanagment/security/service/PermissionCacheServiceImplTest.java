package com.yasarbilgi.visitormeetingmanagment.security.service;

import com.yasarbilgi.visitormeetingmanagment.security.service.impl.PermissionCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * PermissionCacheServiceImpl için JUnit 5 ve Mockito birim testleri.
 *
 * Desen: Arrange (hazırlık) -> Act (işlemi çalıştır) -> Assert (doğrula)
 */
@ExtendWith(MockitoExtension.class)
class PermissionCacheServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PermissionCacheServiceImpl permissionCacheService;

    private static final Long USER_ID = 42L;
    private static final String CACHE_KEY = "permissions:user:42";

    @BeforeEach
    void setUp() {
        // RedisTemplate.opsForValue() çağrıldığında mock ValueOperations dönecek şekilde ayarlıyoruz
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ----- cachePermissions() -----

    @Test
    void cachePermissions_shouldStoreSerializedPermissionsInRedis() {
        Set<String> permissions = Set.of("MEETING_ROOM_VIEW", "RESERVATION_CREATE");

        permissionCacheService.cachePermissions(USER_ID, permissions);

        // İzinlerin virgülle birleştirilerek 30 dakika TTL ile kaydedildiğini doğruluyoruz
        verify(valueOperations, times(1)).set(
                eq(CACHE_KEY),
                argThat(val -> val.contains("MEETING_ROOM_VIEW") && val.contains("RESERVATION_CREATE")),
                eq(Duration.ofMinutes(30))
        );
    }

    // ----- getCachedPermissions() -----

    @Test
    void getCachedPermissions_shouldReturnDeserializedSet_whenValueExistsInRedis() {
        String redisValue = "MEETING_ROOM_VIEW,RESERVATION_CREATE";
        when(valueOperations.get(CACHE_KEY)).thenReturn(redisValue);

        Set<String> result = permissionCacheService.getCachedPermissions(USER_ID);

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder("MEETING_ROOM_VIEW", "RESERVATION_CREATE");
    }

    @Test
    void getCachedPermissions_shouldReturnEmptySet_whenKeyDoesNotExist() {
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);

        Set<String> result = permissionCacheService.getCachedPermissions(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getCachedPermissions_shouldReturnEmptySet_whenValueIsBlank() {
        when(valueOperations.get(CACHE_KEY)).thenReturn("   ");

        Set<String> result = permissionCacheService.getCachedPermissions(USER_ID);

        assertThat(result).isEmpty();
    }

    // ----- invalidate() -----

    @Test
    void invalidate_shouldDeleteKeyFromRedis() {
        permissionCacheService.invalidate(USER_ID);

        verify(redisTemplate, times(1)).delete(eq(CACHE_KEY));
    }

    // ----- isCached() -----

    @Test
    void isCached_shouldReturnTrue_whenKeyExists() {
        when(redisTemplate.hasKey(CACHE_KEY)).thenReturn(true);

        boolean result = permissionCacheService.isCached(USER_ID);

        assertThat(result).isTrue();
    }

    @Test
    void isCached_shouldReturnFalse_whenKeyDoesNotExist() {
        when(redisTemplate.hasKey(CACHE_KEY)).thenReturn(false);

        boolean result = permissionCacheService.isCached(USER_ID);

        assertThat(result).isFalse();
    }

    @Test
    void isCached_shouldReturnFalse_whenHasKeyReturnsNull() {
        when(redisTemplate.hasKey(CACHE_KEY)).thenReturn(null);

        boolean result = permissionCacheService.isCached(USER_ID);

        assertThat(result).isFalse();
    }
}
