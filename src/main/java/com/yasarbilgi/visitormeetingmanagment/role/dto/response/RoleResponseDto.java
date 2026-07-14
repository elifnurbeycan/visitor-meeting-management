package com.yasarbilgi.visitormeetingmanagment.role.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponseDto {

    private Long id;
    private String name;
    private String description;
    private boolean systemRole;
}
