package com.yasarbilgi.visitormeetingmanagment.role.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record RoleTemplateResponseDto(

        Long id,
        String name,
        String description,
        Set<PermissionSummary> permissions,
        boolean active

) {

    @Builder
    public record PermissionSummary(
            Long id,
            String code,
            String name
    ) {
    }
}