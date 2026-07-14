package com.yasarbilgi.visitormeetingmanagment.role.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponse_dto {

    private Long id;
    private String name;
    private String description;
    private boolean systemRole;
}
