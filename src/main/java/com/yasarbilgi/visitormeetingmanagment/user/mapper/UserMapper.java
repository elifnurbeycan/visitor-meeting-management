package com.yasarbilgi.visitormeetingmanagment.user.mapper;

import com.yasarbilgi.visitormeetingmanagment.job.entity.JobTitle;
import com.yasarbilgi.visitormeetingmanagment.role.entity.Role;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;

import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "jobTitle", source = "jobTitle")
    @Mapping(target = "roles", source = "roles")
    UserResponseDto toResponseDto(User user);

    UserResponseDto.JobTitleSummary toJobTitleSummary(JobTitle jobTitle);

    UserResponseDto.RoleSummary toRoleSummary(Role role);

    Set<UserResponseDto.RoleSummary> toRoleSummarySet(Set<Role> roles);

}