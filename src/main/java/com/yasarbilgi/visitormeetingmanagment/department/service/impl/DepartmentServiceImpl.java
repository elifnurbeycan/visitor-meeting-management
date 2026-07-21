package com.yasarbilgi.visitormeetingmanagment.department.service.impl;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.company.entity.Company;
import com.yasarbilgi.visitormeetingmanagment.company.repository.CompanyRepository;
import com.yasarbilgi.visitormeetingmanagment.department.dto.request.DepartmentRequestDto;
import com.yasarbilgi.visitormeetingmanagment.department.dto.response.DepartmentResponseDto;
import com.yasarbilgi.visitormeetingmanagment.department.entity.Department;
import com.yasarbilgi.visitormeetingmanagment.department.mapper.DepartmentMapper;
import com.yasarbilgi.visitormeetingmanagment.department.repository.DepartmentRepository;
import com.yasarbilgi.visitormeetingmanagment.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponseDto create(Long companyId, DepartmentRequestDto dto) {
        log.info("Creating department: {} for company: {}", dto.name(), companyId);

        validateNameNotTaken(companyId, dto.name());

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        Department department = Department.builder()
                .company(company)
                .name(dto.name())
                .description(dto.description())
                .build();

        Department saved = departmentRepository.save(department);

        log.info("Department created successfully with id: {}", saved.getId());
        return departmentMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public DepartmentResponseDto update(Long companyId, Long departmentId, DepartmentRequestDto dto) {
        log.info("Updating department: {} for company: {}", departmentId, companyId);

        Department department = findDepartmentOrThrow(companyId, departmentId);

        if (!department.getName().equals(dto.name())) {
            validateNameNotTaken(companyId, dto.name());
        }

        department.rename(dto.name());
        department.updateDescription(dto.description());

        log.info("Department updated successfully: {}", departmentId);
        return departmentMapper.toResponseDto(department);
    }

    @Override
    public DepartmentResponseDto getById(Long companyId, Long departmentId) {
        Department department = findDepartmentOrThrow(companyId, departmentId);
        return departmentMapper.toResponseDto(department);
    }

    @Override
    public Page<DepartmentResponseDto> getAll(Long companyId, Pageable pageable) {
        return departmentRepository.findAllByCompanyId(companyId, pageable)
                .map(departmentMapper::toResponseDto);
    }

    @Override
    public Page<DepartmentResponseDto> getAllByActive(Long companyId, boolean active, Pageable pageable) {
        return departmentRepository.findAllByCompanyIdAndActive(companyId, active, pageable)
                .map(departmentMapper::toResponseDto);
    }

    @Override
    public Page<DepartmentResponseDto> search(Long companyId, boolean active, String keyword, Pageable pageable) {
        return departmentRepository.searchByKeyword(companyId, active, keyword, pageable)
                .map(departmentMapper::toResponseDto);
    }

    @Override
    @Transactional
    public void deactivate(Long companyId, Long departmentId) {
        log.info("Deactivating department: {} for company: {}", departmentId, companyId);
        Department department = findDepartmentOrThrow(companyId, departmentId);
        department.deactivate();
    }

    @Override
    @Transactional
    public void activate(Long companyId, Long departmentId) {
        log.info("Activating department: {} for company: {}", departmentId, companyId);
        Department department = findDepartmentOrThrow(companyId, departmentId);
        department.activate();
    }

    private Department findDepartmentOrThrow(Long companyId, Long departmentId) {
        return departmentRepository.findByCompanyIdAndId(companyId, departmentId)
                .orElseThrow(() -> {
                    log.warn("Department not found: {} for company: {}", departmentId, companyId);
                    return new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
                });
    }

    private void validateNameNotTaken(Long companyId, String name) {
        if (departmentRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new BusinessException(ErrorCode.DEPARTMENT_ALREADY_EXISTS);
        }
    }
}