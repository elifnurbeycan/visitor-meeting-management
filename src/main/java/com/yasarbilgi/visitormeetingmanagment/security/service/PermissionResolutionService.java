package com.yasarbilgi.visitormeetingmanagment.security.service;

import java.util.Set;

public interface PermissionResolutionService {

    Set<String> resolveEffectivePermissions(Long userId);

}