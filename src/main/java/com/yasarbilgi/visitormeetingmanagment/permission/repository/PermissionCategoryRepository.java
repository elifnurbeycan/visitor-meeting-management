package com.yasarbilgi.visitormeetingmanagment.permission.repository;

import com.yasarbilgi.visitormeetingmanagment.permission.entity.PermissionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionCategoryRepository extends JpaRepository<PermissionCategory, Long> {

    Optional<PermissionCategory> findByCode(String code);

    boolean existsByCode(String code);
}
