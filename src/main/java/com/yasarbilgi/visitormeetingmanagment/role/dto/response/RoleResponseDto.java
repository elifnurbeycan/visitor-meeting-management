package com.yasarbilgi.visitormeetingmanagment.role.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record RoleResponseDto(

        Long id,
        String name,
        String description,
        Set<PermissionSummary> permissions

) {

    @Builder
    public record PermissionSummary(
            Long id,
            String code,
            String name
    ) {
    }
}