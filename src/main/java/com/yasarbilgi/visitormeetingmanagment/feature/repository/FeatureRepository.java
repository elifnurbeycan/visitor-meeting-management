package com.yasarbilgi.visitormeetingmanagment.feature.repository;

import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureRepository extends JpaRepository<Feature, Long> {

    boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);

}
