package com.yasarbilgi.visitormeetingmanagment.common.idempotency.repository;

import com.yasarbilgi.visitormeetingmanagment.common.idempotency.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByIdempotencyKeyAndCompanyIdAndUserId(
            String idempotencyKey, Long companyId, Long userId
    );
}