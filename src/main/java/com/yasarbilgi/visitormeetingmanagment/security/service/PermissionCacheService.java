package com.yasarbilgi.visitormeetingmanagment.security.service;

import java.util.Set;

public interface PermissionCacheService {

    void cachePermissions(Long userId, Set<String> permissions);

    Set<String> getCachedPermissions(Long userId);

    void invalidate(Long userId);

    boolean isCached(Long userId);

}