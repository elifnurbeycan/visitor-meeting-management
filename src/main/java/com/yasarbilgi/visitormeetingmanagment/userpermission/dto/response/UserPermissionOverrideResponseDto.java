package com.yasarbilgi.visitormeetingmanagment.userpermission.dto.response;

import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.OverrideType;
import lombok.Builder;

@Builder
public record UserPermissionOverrideResponseDto(

        Long id,

        Long userId,

        PermissionSummary permission,

        OverrideType type,

        boolean active
) {

    @Builder
    public record PermissionSummary(
            Long id,
            String code,
            String name
    ) {}
}
