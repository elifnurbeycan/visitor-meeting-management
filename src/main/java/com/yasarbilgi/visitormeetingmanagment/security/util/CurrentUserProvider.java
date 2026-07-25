package com.yasarbilgi.visitormeetingmanagment.security.util;

import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * SecurityContext'ten şu anki isteği yapan AuthenticatedUser'ı okumak için
 * merkezi, tekrar kullanılabilir bir yardımcı. AuditorAwareImpl ile aynı
 * SecurityContextHolder deseni kullanılır.
 */
@Component
public class CurrentUserProvider {

    public Optional<AuthenticatedUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
            return Optional.of(authenticatedUser);
        }

        return Optional.empty();
    }
}