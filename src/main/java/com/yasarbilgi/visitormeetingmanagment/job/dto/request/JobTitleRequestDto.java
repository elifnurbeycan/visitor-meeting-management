package com.yasarbilgi.visitormeetingmanagment.job.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobTitleRequestDto {

    @NotBlank(message = "Job title name cannot be empty")
    @Size(max = 100, message = "Job title name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Job title description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Default role information must be specified")
    private Boolean hasDefaultRole;
}