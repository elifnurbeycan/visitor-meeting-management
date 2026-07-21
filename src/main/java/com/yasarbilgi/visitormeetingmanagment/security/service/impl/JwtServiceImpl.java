package com.yasarbilgi.visitormeetingmanagment.security.service.impl;

import com.yasarbilgi.visitormeetingmanagment.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT üretimi ve doğrulaması. Access token'lar "ince" tasarlanmıştır —
 * sadece userId, companyId ve permissions claim'lerini taşır. Efektif
 * izinler ayrıca Redis'te de cache'lenir (PermissionCacheService); JWT'deki
 * izin listesi, token süresi boyunca (15 dk) sabit kalır, ama gerçek
 * yetki kontrolleri filter seviyesinde Redis'ten okunan güncel listeye
 * göre yapılabilir — bu tasarım kararı SecurityConfig'te netleştirilecek.
 *
 * Refresh token'lar ise JWT değil, rastgele üretilmiş opak (opaque)
 * string'lerdir — veritabanında hash'lenerek saklanır (RefreshToken entity).
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private static final String CLAIM_COMPANY_ID = "companyId";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_USER = "USER";
    private static final String TOKEN_TYPE_SUPER_ADMIN = "SUPER_ADMIN";
    private static final String PERMISSION_DELIMITER = ",";

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    @Override
    public String generateAccessToken(Long userId, Long companyId, Set<String> permissions) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        String permissionsClaim = String.join(PERMISSION_DELIMITER, permissions);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_COMPANY_ID, companyId)
                .claim(CLAIM_PERMISSIONS, permissionsClaim)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_USER)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String generateSuperAdminAccessToken(Long superAdminId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(superAdminId))
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_SUPER_ADMIN)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String generateRefreshToken() {
        // Opak (JWT olmayan), kriptografik olarak güvenli rastgele token
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Long extractUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    @Override
    public Long extractCompanyId(String token) {
        Object companyId = parseClaims(token).get(CLAIM_COMPANY_ID);
        return companyId != null ? Long.valueOf(companyId.toString()) : null;
    }

    @Override
    public Set<String> extractPermissions(String token) {
        String permissionsClaim = parseClaims(token).get(CLAIM_PERMISSIONS, String.class);
        if (permissionsClaim == null || permissionsClaim.isBlank()) {
            return Set.of();
        }
        return Set.of(permissionsClaim.split(PERMISSION_DELIMITER)).stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isSuperAdminToken(String token) {
        String tokenType = parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
        return TOKEN_TYPE_SUPER_ADMIN.equals(tokenType);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}