package com.yasarbilgi.visitormeetingmanagment.department.dto.response;

import lombok.Builder;

@Builder
public record DepartmentResponseDto(

        Long id,
        String name,
        String description,
        int userCount,
        boolean active

) {
}