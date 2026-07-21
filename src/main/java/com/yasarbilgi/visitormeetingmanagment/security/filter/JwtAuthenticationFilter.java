package com.yasarbilgi.visitormeetingmanagment.security.filter;

import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.security.service.JwtService;
import com.yasarbilgi.visitormeetingmanagment.security.service.PermissionCacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Her istekte Authorization header'ındaki JWT'yi doğrular. Geçerliyse,
 * kullanıcı bilgisini (AuthenticatedUser) SecurityContext'e koyar —
 * hem @PreAuthorize kontrolleri hem de AuditorAwareImpl bu bilgiyi kullanır.
 *
 * Efektif izinler, Spring'in standart GrantedAuthority listesine de
 * kopyalanır — böylece @PreAuthorize("hasAuthority('ROOM_CREATE')") gibi
 * standart Spring Security kontrolleri, bizim Redis/JWT'den okuduğumuz
 * izin listesiyle doğrudan çalışabilir.
 *
 * SuperAdmin token'ları için ayrıca "ROLE_SUPER_ADMIN" authority'si eklenir,
 * bu sayede @PreAuthorize("hasRole('SUPER_ADMIN')") kullanılabilir.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

    private final JwtService jwtService;
    private final PermissionCacheService permissionCacheService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null || !jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthenticatedUser authenticatedUser = buildAuthenticatedUser(token);
            List<GrantedAuthority> authorities = buildAuthorities(authenticatedUser);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.warn("Failed to authenticate request: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * AuthenticatedUser'daki izinleri (ve SuperAdmin ise rol bilgisini)
     * Spring Security'nin standart GrantedAuthority listesine çevirir.
     */
    private List<GrantedAuthority> buildAuthorities(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.superAdmin()) {
            return List.of(new SimpleGrantedAuthority(SUPER_ADMIN_ROLE));
        }

        return authenticatedUser.permissions().stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private AuthenticatedUser buildAuthenticatedUser(String token) {
        if (jwtService.isSuperAdminToken(token)) {
            Long superAdminId = jwtService.extractUserId(token);
            return AuthenticatedUser.builder()
                    .userId(superAdminId)
                    .superAdmin(true)
                    .permissions(Set.of())
                    .build();
        }

        Long userId = jwtService.extractUserId(token);
        Long companyId = jwtService.extractCompanyId(token);

        Set<String> permissions = permissionCacheService.getCachedPermissions(userId);
        if (permissions.isEmpty()) {
            permissions = jwtService.extractPermissions(token);
        }

        return AuthenticatedUser.builder()
                .userId(userId)
                .companyId(companyId)
                .permissions(permissions)
                .superAdmin(false)
                .build();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}