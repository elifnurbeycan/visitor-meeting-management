package com.yasarbilgi.visitormeetingmanagment.job.mapper;

import com.yasarbilgi.visitormeetingmanagment.job.dto.response.JobTitleResponseDto;
import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface JobTitleMapper {

    @Mapping(target = "defaultRoles", source = "defaultRoles")
    JobTitleResponseDto toResponseDto(JobTitle jobTitle);

    JobTitleResponseDto.RoleSummary toRoleSummary(Role role);

    Set<JobTitleResponseDto.RoleSummary> toRoleSummarySet(Set<Role> roles);

}