package com.yasarbilgi.visitormeetingmanagment.common.response;

import java.time.Instant;

/**
 * API'de oluşan hataların istemciye standart bir formatta dönülmesini sağlar.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path
) {
}