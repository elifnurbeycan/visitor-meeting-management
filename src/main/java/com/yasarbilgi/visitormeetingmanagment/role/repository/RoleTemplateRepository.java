package com.yasarbilgi.visitormeetingmanagment.role.repository;

import com.yasarbilgi.visitormeetingmanagment.role.entity.RoleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleTemplateRepository extends JpaRepository<RoleTemplate, Long> {
    List<RoleTemplate> findAllByActiveTrue();
}