package com.yasarbilgi.visitormeetingmanagment.common.config;

import com.yasarbilgi.visitormeetingmanagment.security.interceptor.TenantFilterInterceptor;
import com.yasarbilgi.visitormeetingmanagment.security.interceptor.TenantPathGuardInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantFilterInterceptor tenantFilterInterceptor;
    private final TenantPathGuardInterceptor tenantPathGuardInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(tenantPathGuardInterceptor).order(50);
        registry.addInterceptor(tenantFilterInterceptor).order(100);

    }
}