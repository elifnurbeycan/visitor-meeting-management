package com.yasarbilgi.visitormeetingmanagment.common.ratelimit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.common.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Login endpoint'lerine (şirket kullanıcısı ve SuperAdmin) karşı IP bazlı
 * brute-force koruması sağlar. Redis'te "ratelimit:login:{ip}:{path}"
 * anahtarıyla, SADECE BAŞARISIZ (401 Unauthorized ile sonuçlanan) deneme
 * sayısını tutan bir sayaç tutar. Belirlenen zaman penceresi içinde izin
 * verilen başarısız deneme sayısı aşılırsa isteği 429 (Too Many Requests)
 * ile reddeder.
 * <p>
 * Başarılı login'ler sayaca dahil edilmez — amaç, aynı IP'den art arda
 * yanlış şifre denemesini engellemektir; meşru kullanıcıların (özellikle
 * aynı IP'yi paylaşan bir ofis/NAT arkasındaki birden fazla kişinin) sık
 * sık başarılı giriş yapması bu mekanizmayı tetiklemez.

 * Redis'e erişilemezse (bkz. PermissionCacheServiceImpl'deki aynı prensip),
 * bu filtre güvenli tarafta hata yapar: rate limit uygulanamıyorsa isteği
 * engellemek yerine geçirir — çünkü bu bir savunma katmanıdır, sistemin
 * çekirdek işlevi değildir; Redis kesintisinde login akışının tamamen
 * durmasına izin vermeyiz.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/platform/auth/login"
    );
    private static final String KEY_PREFIX = "ratelimit:login:";
    private static final int FAILED_LOGIN_STATUS = 401;

    private final RedisTemplate<String, String> redisTemplate;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    @Value("${rate-limit.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${rate-limit.login.window-seconds:60}")
    private long windowSeconds;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        boolean isProtectedPath = PROTECTED_PATHS.contains(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod());

        if (!isProtectedPath) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = KEY_PREFIX + resolveClientIp(request) + ":" + request.getRequestURI();

        if (isAlreadyBlocked(key)) {
            log.warn(
                    "Rate limit aşıldı, istek denenmeden reddedildi: key={}, path={}",
                    key, request.getRequestURI()
            );
            writeTooManyRequests(request, response);
            return;
        }

        filterChain.doFilter(request, response);

        if (response.getStatus() == FAILED_LOGIN_STATUS) {
            registerFailedAttempt(key);
        }
    }

    /**
     * İstek işlenmeden ÖNCE mevcut başarısız deneme sayısını okur (artırmadan).
     * Sayaç zaten limitteyse isteği hiç login akışına sokmadan reddeder.
     */
    private boolean isAlreadyBlocked(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null && Long.parseLong(value) >= maxAttempts;
        } catch (DataAccessException e) {
            log.warn(
                    "Rate limit kontrolü yapılamadı (Redis erişilemiyor), istek engellenmeden geçiriliyor: {}",
                    e.getMessage()
            );
            return false;
        }
    }

    /**
     * İstek SONUÇLANDIKTAN SONRA, eğer sonuç 401 (yanlış kimlik bilgisi) ise
     * çağrılır. Sayacı bir artırır; ilk artırımda pencere süresi kadar TTL koyar.
     */
    private void registerFailedAttempt(String key) {
        try {
            Long attempts = redisTemplate.opsForValue().increment(key);

            if (attempts != null && attempts == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }

            if (attempts != null) {
                log.debug("Başarısız login denemesi kaydedildi: key={}, attempts={}", key, attempts);
            }
        } catch (DataAccessException e) {
            log.warn(
                    "Başarısız deneme Redis'e kaydedilemedi (Redis erişilemiyor): {}",
                    e.getMessage()
            );
        }
    }

    private void writeTooManyRequests(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String message = messageSource.getMessage(
                ErrorCode.TOO_MANY_REQUESTS.getMessageKey(),
                null,
                request.getLocale()
        );

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ErrorCode.TOO_MANY_REQUESTS.getHttpStatus().value(),
                ErrorCode.TOO_MANY_REQUESTS.name(),
                message,
                request.getRequestURI()
        );

        response.setStatus(ErrorCode.TOO_MANY_REQUESTS.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Reverse proxy arkasında çalışırken (örn. nginx, AWS ALB) gerçek istemci
     * IP'si X-Forwarded-For header'ında taşınır. Bu header yalnızca GÜVENİLİR
     * bir proxy tarafından set edildiğinde anlamlıdır — aksi halde istemci bu
     * header'ı kendisi göndererek rate limit'i atlatabilir (her istekte farklı
     * bir IP iddia edip sayaç anahtarını değiştirebilir). Production'a çıkarken
     * bu filtrenin yalnızca bilinen/güvenilir bir proxy'nin arkasında
     * çalıştığından emin olunmalı; aksi halde bu header tamamen yok sayılıp
     * sadece request.getRemoteAddr() kullanılmalı.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}