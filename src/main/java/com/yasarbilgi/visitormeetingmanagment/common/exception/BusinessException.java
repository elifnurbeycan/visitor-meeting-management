package com.yasarbilgi.visitormeetingmanagment.common.exception;

import lombok.Getter;

/**
 * Uygulamadaki kontrollü hata durumlarını ve iş kuralı ihlallerini
 * temsil eden ortak exception sınıfıdır.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.args = args;
    }
}