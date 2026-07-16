package com.yasarbilgi.visitormeetingmanagment.userpermission.dto.request;

import com.yasarbilgi.visitormeetingmanagment.userpermission.entity.OverrideType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record UserPermissionOverrideRequestDto(

        @NotNull(message = "{userPermissionOverride.userId.required}")
        Long userId,

        @NotNull(message = "{userPermissionOverride.permissionId.required}")
        Long permissionId,

        @NotNull(message = "{userPermissionOverride.type.required}")
        OverrideType type
) {}
