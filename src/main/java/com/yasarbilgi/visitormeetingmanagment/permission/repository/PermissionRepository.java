package com.yasarbilgi.visitormeetingmanagment.permission.repository;

import com.yasarbilgi.visitormeetingmanagment.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

}
