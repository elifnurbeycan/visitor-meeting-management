package com.yasarbilgi.visitormeetingmanagment.company.dto.response;

import com.yasarbilgi.visitormeetingmanagment.platform.enums.CompanyStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record CompanyResponseDto(

        Long id,

        String name,

        String slug,

        String description,

        String taxNumber,

        String contactEmail,

        String contactPhone,

        String address,

        String industry,

        CompanyStatus status,

        String rejectionReason,

        boolean active,

        Instant createdAt,

        Instant updatedAt

) {
}