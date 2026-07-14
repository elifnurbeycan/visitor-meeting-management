package com.yasarbilgi.visitormeetingmanagment.job.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobTitleResponseDto {

    private Long id;

    private String name;

    private String description;

    private Boolean hasDefaultRole;

    private Boolean active;
}