package com.yasarbilgi.visitormeetingmanagment.common.handler;

import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.common.response.ErrorResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Uygulama genelinde oluşan exception'ları yakalar
 * ve standart HTTP hata cevaplarına dönüştürür.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request,
            Locale locale
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        String message = messageSource.getMessage(
                errorCode.getMessageKey(),
                exception.getArgs(),
                locale
        );

        log.warn("BusinessException: {} - {}", errorCode.name(), message);

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                errorCode.getHttpStatus().value(),
                errorCode.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request,
            Locale locale
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() != null
                            ? fieldError.getDefaultMessage()
                            : "Invalid value"
            );
        }

        log.warn("Validation failed: {}", fieldErrors);

        String message = messageSource.getMessage(
                ErrorCode.VALIDATION_FAILED.getMessageKey(),
                null,
                locale
        );

        ValidationErrorResponse response = new ValidationErrorResponse(
                Instant.now(),
                ErrorCode.VALIDATION_FAILED.getHttpStatus().value(),
                ErrorCode.VALIDATION_FAILED.name(),
                message,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request,
            Locale locale
    ) {
        log.warn("Data integrity violation: {}", exception.getMessage());

        String message = messageSource.getMessage(
                ErrorCode.BUSINESS_RULE_VIOLATION.getMessageKey(),
                null,
                locale
        );

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                ErrorCode.BUSINESS_RULE_VIOLATION.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request,
            Locale locale
    ) {
        log.warn("Access denied: {} -> {}", request.getRequestURI(), exception.getMessage());

        String message = messageSource.getMessage(
                ErrorCode.FORBIDDEN.getMessageKey(),
                null,
                locale
        );

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                ErrorCode.FORBIDDEN.getHttpStatus().value(),
                ErrorCode.FORBIDDEN.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Beklenmeyen hata: {}", exception.getMessage(), exception);

        String message = messageSource.getMessage(
                ErrorCode.INTERNAL_SERVER_ERROR.getMessageKey(),
                null,
                locale
        );

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}