package com.yasarbilgi.visitormeetingmanagment.common.config;

import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * BaseEntity'deki @CreatedBy/@LastModifiedBy alanlarını otomatik doldurur.
 * SecurityContext'teki AuthenticatedUser'dan userId'yi çıkarır.
 * Authentication yoksa (örn. login/register gibi anonim endpoint'ler),
 * boş Optional döner — bu durumda createdBy/updatedBy null kalır.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return Optional.of(authenticatedUser.userId());
        }

        return Optional.empty();
    }
}