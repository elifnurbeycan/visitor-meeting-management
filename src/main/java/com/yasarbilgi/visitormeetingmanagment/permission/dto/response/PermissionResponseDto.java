package com.yasarbilgi.visitormeetingmanagment.permission.dto.response;

import com.yasarbilgi.visitormeetingmanagment.permission.entity.PermissionCategory;
import lombok.Builder;

@Builder
public record PermissionResponseDto(

        Long id,
        String code,
        String name,
        String description,
        PermissionCategory category,
        boolean systemPermission,
        int displayOrder,
        boolean active

) {
}