package com.yasarbilgi.visitormeetingmanagment.security.interceptor;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/**
 * URL path'inde ya da query parametresinde companyId taşıyan her endpoint için
 * (örn. /api/v1/companies/{companyId}/users ya da /api/v1/roles?companyId=7),
 * gönderilen companyId'nin gerçekten isteği yapan kullanıcının kendi
 * şirketiyle eşleştiğini doğrular. Eşleşmezse TENANT_ACCESS_DENIED fırlatır.
 *
 * Bu, servis katmanındaki manuel companyId filtrelemesine ek olarak,
 * "companyId'yi elle değiştirip başka şirketin verisine erişme" (IDOR)
 * saldırısını controller seviyesinde, sorgu hiç çalışmadan önce engeller.
 *
 * SuperAdmin bu kontrolden muaftır (şirketler arası erişimi doğaldır).
 */
@Slf4j
@Component
public class TenantPathGuardInterceptor implements HandlerInterceptor {

    private static final String COMPANY_ID_PATH_VARIABLE = "companyId";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        Long requestedCompanyId = extractRequestedCompanyId(request);

        if (requestedCompanyId == null) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return true;
        }

        if (user.superAdmin()) {
            return true;
        }

        if (!requestedCompanyId.equals(user.companyId())) {
            log.warn(
                    "Tenant guard reddetti: userId={}, ownCompanyId={}, istenenCompanyId={}, uri={}",
                    user.userId(), user.companyId(), requestedCompanyId, request.getRequestURI()
            );
            throw new BusinessException(ErrorCode.TENANT_ACCESS_DENIED);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private Long extractRequestedCompanyId(HttpServletRequest request) {
        Map<String, String> pathVariables = (Map<String, String>)
                request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVariables != null && pathVariables.containsKey(COMPANY_ID_PATH_VARIABLE)) {
            return Long.valueOf(pathVariables.get(COMPANY_ID_PATH_VARIABLE));
        }

        String queryParam = request.getParameter(COMPANY_ID_PATH_VARIABLE);
        if (queryParam != null && !queryParam.isBlank()) {
            return Long.valueOf(queryParam);
        }

        return null;
    }
}