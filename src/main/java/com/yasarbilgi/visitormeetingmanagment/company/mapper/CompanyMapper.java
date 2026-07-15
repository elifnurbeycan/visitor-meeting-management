package com.yasarbilgi.visitormeetingmanagment.company.mapper;

import com.yasarbilgi.visitormeetingmanagment.company.dto.request.CompanyRequestDto;
import com.yasarbilgi.visitormeetingmanagment.company.dto.response.CompanyResponseDto;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponseDto toResponseDto(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deactivatedAt", ignore = true)
    Company toEntity(CompanyRequestDto dto);

}