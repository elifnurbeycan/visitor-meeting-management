package com.yasarbilgi.visitormeetingmanagment.security.interceptor;

import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * DispatcherServlet'in interceptor zincirinde, OpenEntityManagerInViewInterceptor
 * (OSIV) çalıştıktan SONRA tetiklenir — bu noktada thread'e gerçek/kalıcı bir
 * Hibernate Session zaten bağlanmış olur. SecurityContext'teki AuthenticatedUser'dan
 * companyId'yi okuyup tenantFilter'ı bu GERÇEK session üzerinde enable eder.
 *
 * Not: Bu daha önce bir Filter olarak yazılmıştı, ama Filter'lar OSIV'den ÖNCE
 * çalıştığı için, o zaman enable edilen filtre geçici (transient) bir
 * EntityManager üzerinde uygulanıyor ve anında etkisiz kalıyordu.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilterInterceptor implements HandlerInterceptor {

    private final EntityManager entityManager;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user
                && !user.superAdmin() && user.companyId() != null) {

            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", user.companyId());
            log.debug("tenantFilter enabled for tenantId={}", user.companyId());
        }

        return true;
    }
}