package com.yasarbilgi.visitormeetingmanagment.auth.service;

import com.yasarbilgi.visitormeetingmanagment.security.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceImplTest {

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 60_000L;

    private static final String RAW_SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";

    private static final String BASE64_SECRET =
            Base64.getEncoder().encodeToString(
                    RAW_SECRET.getBytes(StandardCharsets.UTF_8)
            );

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(
                BASE64_SECRET,
                ACCESS_TOKEN_EXPIRATION_MS
        );
    }

    // ----- generateAccessToken() -----

    @Test
    void generateAccessToken_shouldCreateValidUserToken() {
        Long userId = 10L;
        Long companyId = 20L;
        Set<String> permissions = Set.of(
                "ROOM_VIEW",
                "USER_CREATE"
        );

        String token = jwtService.generateAccessToken(
                userId,
                companyId,
                permissions
        );

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractCompanyId(token)).isEqualTo(companyId);
        assertThat(jwtService.extractPermissions(token))
                .containsExactlyInAnyOrderElementsOf(permissions);
        assertThat(jwtService.isSuperAdminToken(token)).isFalse();
    }

    @Test
    void generateAccessToken_shouldSupportEmptyPermissions() {
        String token = jwtService.generateAccessToken(
                10L,
                20L,
                Set.of()
        );

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractPermissions(token)).isEmpty();
    }

    @Test
    void generateAccessToken_shouldSupportNullCompanyId() {
        String token = jwtService.generateAccessToken(
                10L,
                null,
                Set.of("ROOM_VIEW")
        );

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractCompanyId(token)).isNull();
    }

    // ----- generateSuperAdminAccessToken() -----

    @Test
    void generateSuperAdminAccessToken_shouldCreateValidSuperAdminToken() {
        Long superAdminId = 99L;

        String token =
                jwtService.generateSuperAdminAccessToken(superAdminId);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token))
                .isEqualTo(superAdminId);
        assertThat(jwtService.extractCompanyId(token)).isNull();
        assertThat(jwtService.extractPermissions(token)).isEmpty();
        assertThat(jwtService.isSuperAdminToken(token)).isTrue();
    }

    // ----- generateRefreshToken() -----

    @Test
    void generateRefreshToken_shouldReturnNonBlankOpaqueToken() {
        String token = jwtService.generateRefreshToken();

        assertThat(token).isNotBlank();
        assertThat(token).doesNotContain(".");
    }

    @Test
    void generateRefreshToken_shouldGenerateDifferentTokens() {
        String firstToken = jwtService.generateRefreshToken();
        String secondToken = jwtService.generateRefreshToken();

        assertThat(firstToken).isNotEqualTo(secondToken);
    }

    @Test
    void generateRefreshToken_shouldHaveExpectedDecodedLength() {
        String token = jwtService.generateRefreshToken();

        byte[] decoded =
                Base64.getUrlDecoder().decode(token);

        assertThat(decoded).hasSize(64);
    }

    // ----- isTokenValid() -----

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        String token = jwtService.generateAccessToken(
                10L,
                20L,
                Set.of("ROOM_VIEW")
        );

        boolean result = jwtService.isTokenValid(token);

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsMalformed() {
        boolean result =
                jwtService.isTokenValid("invalid-token");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsNull() {
        boolean result = jwtService.isTokenValid(null);

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsBlank() {
        boolean result = jwtService.isTokenValid("");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenHasDifferentSignature() {
        JwtServiceImpl otherJwtService = new JwtServiceImpl(
                Base64.getEncoder().encodeToString(
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789ab"
                                .getBytes(StandardCharsets.UTF_8)
                ),
                ACCESS_TOKEN_EXPIRATION_MS
        );

        String token = otherJwtService.generateAccessToken(
                10L,
                20L,
                Set.of("ROOM_VIEW")
        );

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        JwtServiceImpl expiredJwtService =
                new JwtServiceImpl(BASE64_SECRET, -1L);

        String token = expiredJwtService.generateAccessToken(
                10L,
                20L,
                Set.of("ROOM_VIEW")
        );

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    // ----- extractUserId() -----

    @Test
    void extractUserId_shouldReturnSubjectAsLong() {
        String token = jwtService.generateAccessToken(
                123L,
                20L,
                Set.of()
        );

        Long result = jwtService.extractUserId(token);

        assertThat(result).isEqualTo(123L);
    }

    // ----- extractCompanyId() -----

    @Test
    void extractCompanyId_shouldReturnCompanyId() {
        String token = jwtService.generateAccessToken(
                10L,
                456L,
                Set.of()
        );

        Long result = jwtService.extractCompanyId(token);

        assertThat(result).isEqualTo(456L);
    }

    @Test
    void extractCompanyId_shouldReturnNull_forSuperAdminToken() {
        String token =
                jwtService.generateSuperAdminAccessToken(99L);

        Long result = jwtService.extractCompanyId(token);

        assertThat(result).isNull();
    }

    // ----- extractPermissions() -----

    @Test
    void extractPermissions_shouldReturnAllPermissions() {
        Set<String> permissions = Set.of(
                "ROOM_VIEW",
                "USER_CREATE",
                "COMPANY_UPDATE"
        );

        String token = jwtService.generateAccessToken(
                10L,
                20L,
                permissions
        );

        Set<String> result =
                jwtService.extractPermissions(token);

        assertThat(result)
                .containsExactlyInAnyOrderElementsOf(permissions);
    }

    @Test
    void extractPermissions_shouldReturnEmptySet_whenClaimIsMissing() {
        String token =
                jwtService.generateSuperAdminAccessToken(99L);

        Set<String> result =
                jwtService.extractPermissions(token);

        assertThat(result).isEmpty();
    }

    @Test
    void extractPermissions_shouldIgnoreBlankValues() {
        SecretKey signingKey = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(BASE64_SECRET)
        );

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 60_000L);

        String token = Jwts.builder()
                .subject("10")
                .claim("companyId", 20L)
                .claim("permissions", "ROOM_VIEW,,USER_CREATE,")
                .claim("tokenType", "USER")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();

        Set<String> result =
                jwtService.extractPermissions(token);

        assertThat(result).containsExactlyInAnyOrder(
                "ROOM_VIEW",
                "USER_CREATE"
        );
    }

    // ----- isSuperAdminToken() -----

    @Test
    void isSuperAdminToken_shouldReturnTrue_forSuperAdminToken() {
        String token =
                jwtService.generateSuperAdminAccessToken(99L);

        assertThat(jwtService.isSuperAdminToken(token)).isTrue();
    }

    @Test
    void isSuperAdminToken_shouldReturnFalse_forUserToken() {
        String token = jwtService.generateAccessToken(
                10L,
                20L,
                Set.of()
        );

        assertThat(jwtService.isSuperAdminToken(token)).isFalse();
    }
}