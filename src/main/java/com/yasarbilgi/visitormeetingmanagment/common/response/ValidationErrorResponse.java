package com.yasarbilgi.visitormeetingmanagment.common.response;

import java.time.Instant;
import java.util.Map;

/**
 * Alan bazlı validasyon hatalarını (örn: @NotBlank, @Email ihlalleri)
 * istemciye field -> mesaj eşlemesiyle standart formatta döner.
 */
public record ValidationErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}