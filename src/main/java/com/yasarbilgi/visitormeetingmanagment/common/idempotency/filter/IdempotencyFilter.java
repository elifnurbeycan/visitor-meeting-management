package com.yasarbilgi.visitormeetingmanagment.common.idempotency.filter;

import com.yasarbilgi.visitormeetingmanagment.common.idempotency.entity.IdempotencyKey;
import com.yasarbilgi.visitormeetingmanagment.common.idempotency.repository.IdempotencyKeyRepository;
import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * İstemci, bir POST isteğine "Idempotency-Key" header'ı (benzersiz bir UUID)
 * eklerse, bu filtre aynı anahtarla gelen tekrar istekleri fark eder ve
 * işlemi tekrar çalıştırmadan, önceden üretilmiş aynı yanıtı geri döner.
 * <p>
 * Bu mekanizma sadece açıkça belirlenmiş, idempotency gerektiren endpoint'lerde
 * devrededir (şu an sadece rezervasyon oluşturma).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";
    private static final String ELIGIBLE_PATH = "/api/v1/reservations";

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);
        boolean isEligiblePath = ELIGIBLE_PATH.equals(request.getRequestURI());

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || !"POST".equalsIgnoreCase(request.getMethod())
                || !isEligiblePath) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isValidUuid(idempotencyKey)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Idempotency-Key must be a valid UUID\"}"
            );
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication != null
                && authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long companyId = user.companyId();
        Long userId = user.userId();

        Optional<IdempotencyKey> existing = idempotencyKeyRepository
                .findByIdempotencyKeyAndCompanyIdAndUserId(
                        idempotencyKey,
                        companyId,
                        userId
                );

        if (existing.isPresent()) {
            IdempotencyKey cached = existing.get();

            log.info(
                    "Idempotent tekrar istek tespit edildi, cached response dönülüyor: key={}, userId={}",
                    idempotencyKey,
                    userId
            );

            response.setStatus(cached.getResponseStatus());
            response.setContentType("application/json");
            response.getWriter().write(cached.getResponseBody());
            return;
        }

        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        filterChain.doFilter(request, wrappedResponse);

        int status = wrappedResponse.getStatus();

        if (status >= 200 && status < 300) {
            saveIdempotencyRecord(
                    idempotencyKey,
                    companyId,
                    userId,
                    status,
                    wrappedResponse
            );
        }

        wrappedResponse.copyBodyToResponse();
    }

    protected void saveIdempotencyRecord(
            String idempotencyKey,
            Long companyId,
            Long userId,
            int status,
            ContentCachingResponseWrapper wrappedResponse
    ) {
        String bodyString = new String(
                wrappedResponse.getContentAsByteArray(),
                StandardCharsets.UTF_8
        );

        IdempotencyKey record = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .companyId(companyId)
                .userId(userId)
                .responseStatus(status)
                .responseBody(bodyString)
                .build();

        idempotencyKeyRepository.save(record);

        log.debug(
                "Idempotency key kaydedildi: {}, userId={}",
                idempotencyKey,
                userId
        );
    }


    private boolean isValidUuid(String value) {
        try {
            UUID parsedUuid = UUID.fromString(value);
            return parsedUuid.toString().equalsIgnoreCase(value);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}