package com.yasarbilgi.visitormeetingmanagment.department.controller;

import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.department.dto.request.DepartmentRequestDto;
import com.yasarbilgi.visitormeetingmanagment.department.dto.response.DepartmentResponseDto;
import com.yasarbilgi.visitormeetingmanagment.department.service.DepartmentService;
import com.yasarbilgi.visitormeetingmanagment.user.dto.response.UserResponseDto;
import com.yasarbilgi.visitormeetingmanagment.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final UserService userService;


    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> create(
            @PathVariable Long companyId,
            @Valid @RequestBody DepartmentRequestDto dto
    ) {
        DepartmentResponseDto created = departmentService.create(companyId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", created));
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long departmentId,
            @Valid @RequestBody DepartmentRequestDto dto
    ) {
        DepartmentResponseDto updated = departmentService.update(companyId, departmentId, dto);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> getById(
            @PathVariable Long companyId,
            @PathVariable Long departmentId
    ) {
        DepartmentResponseDto department = departmentService.getById(companyId, departmentId);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponseDto>>> getAll(
            @PathVariable Long companyId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<DepartmentResponseDto> departments =
                PageResponse.of(departmentService.getAll(companyId, pageable));
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/by-active")
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponseDto>>> getAllByActive(
            @PathVariable Long companyId,
            @RequestParam boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<DepartmentResponseDto> departments =
                PageResponse.of(departmentService.getAllByActive(companyId, active, pageable));
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponseDto>>> search(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        PageResponse<DepartmentResponseDto> departments =
                PageResponse.of(departmentService.search(companyId, active, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @PatchMapping("/{departmentId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long companyId,
            @PathVariable Long departmentId
    ) {
        departmentService.deactivate(companyId, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated successfully"));
    }

    @PatchMapping("/{departmentId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long companyId,
            @PathVariable Long departmentId
    ) {
        departmentService.activate(companyId, departmentId);
        return ResponseEntity.ok(ApiResponse.success("Department activated successfully"));
    }


    @GetMapping("/{departmentId}/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getUsers(
            @PathVariable Long companyId,
            @PathVariable Long departmentId,
            @PageableDefault(size = 20, sort = "firstName") Pageable pageable
    ) {
        PageResponse<UserResponseDto> users =
                PageResponse.of(userService.getAllByDepartment(companyId, departmentId, pageable));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

}