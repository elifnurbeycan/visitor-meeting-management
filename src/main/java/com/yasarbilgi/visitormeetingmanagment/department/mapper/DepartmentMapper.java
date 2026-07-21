package com.yasarbilgi.visitormeetingmanagment.department.mapper;

import com.yasarbilgi.visitormeetingmanagment.department.dto.response.DepartmentResponseDto;
import com.yasarbilgi.visitormeetingmanagment.department.entity.Department;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "userCount", expression = "java(department.getUserCount())")
    DepartmentResponseDto toResponseDto(Department department);

}