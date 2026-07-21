package com.yasarbilgi.visitormeetingmanagment.security.repository;

import com.yasarbilgi.visitormeetingmanagment.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Bir kullanıcının (User) tüm aktif refresh token'larını iptal eder.
     * Şifre değişikliği, şüpheli aktivite, ya da "tüm cihazlardan çıkış yap"
     * senaryolarında kullanılır.
     */
    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revoked = true, rt.revokedAt = :now
            WHERE rt.user.id = :userId AND rt.revoked = false
            """)
    void revokeAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revoked = true, rt.revokedAt = :now
            WHERE rt.superAdmin.id = :superAdminId AND rt.revoked = false
            """)
    void revokeAllBySuperAdminId(@Param("superAdminId") Long superAdminId, @Param("now") Instant now);

    /**
     * Süresi geçmiş token kayıtlarını temizlemek için (ileride @Scheduled
     * bir job ile periyodik olarak çağrılabilir, tablo şişmesin diye).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteAllExpired(@Param("now") Instant now);

}