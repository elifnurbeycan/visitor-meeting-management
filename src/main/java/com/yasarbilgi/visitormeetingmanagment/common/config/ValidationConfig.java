package com.yasarbilgi.visitormeetingmanagment.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Bean Validation (@NotBlank, @Email, @Size vb.) anotasyonlarının
 * mesajlarını, uygulamanın kendi MessageSource'undan (messages.properties)
 * okumasını sağlar.
 */
@Configuration
@RequiredArgsConstructor
public class ValidationConfig {

    private final MessageSource messageSource;

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}