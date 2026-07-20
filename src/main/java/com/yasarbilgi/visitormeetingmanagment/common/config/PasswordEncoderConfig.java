package com.yasarbilgi.visitormeetingmanagment.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Şifre hashleme için merkezi PasswordEncoder bean tanımı.
 * BCrypt kullanılıyor — endüstri standardı, salt otomatik üretiliyor,
 * hash başına maliyet faktörü (strength) ayarlanabiliyor.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt strength (log rounds) — varsayılan 10, biz 12 kullanıyoruz.
     * Her +1, hash süresini yaklaşık 2 katına çıkarır — brute-force'u
     * zorlaştırır ama login performansını da etkiler. 12, güvenlik/performans
     * dengesi için yaygın kabul gören bir değer (OWASP önerisi 10-12 arası).
     */
    private static final int BCRYPT_STRENGTH = 12;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

}