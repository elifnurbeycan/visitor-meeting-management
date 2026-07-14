package com.yasarbilgi.visitormeetingmanagment.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Uygulamada oluşabilecek kontrollü hata türlerini,
 * HTTP durumlarını ve mesaj anahtarlarını merkezi olarak tutar.
 */
@Getter
public enum ErrorCode {

    // Common
    BUSINESS_RULE_VIOLATION(
            HttpStatus.BAD_REQUEST,
            "business.ruleViolation"
    ),

    // Company
    COMPANY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "company.notFound"
    ),

    COMPANY_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "company.alreadyExists"
    ),

    COMPANY_INACTIVE(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "company.inactive"
    ),

    // Role
    ROLE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "role.notFound"
    ),

    ROLE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "role.alreadyExists"
    ),

    ROLE_INACTIVE(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "role.inactive"
    ),

    // Permission
    PERMISSION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "permission.notFound"
    ),

    PERMISSION_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "permission.alreadyExists"
    ),

    // Job Title
    JOB_TITLE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "jobTitle.notFound"
    ),

    JOB_TITLE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "jobTitle.alreadyExists"
    ),

    // User
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "user.notFound"
    ),

    USER_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "user.alreadyExists"
    ),

    // Meeting Room
    MEETING_ROOM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "meetingRoom.notFound"
    ),

    MEETING_ROOM_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "meetingRoom.alreadyExists"
    ),

    MEETING_ROOM_INACTIVE(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "meetingRoom.inactive"
    ),

    INVALID_ROOM_CAPACITY(
            HttpStatus.BAD_REQUEST,
            "meetingRoom.invalidCapacity"
    ),

    // Reservation
    RESERVATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "reservation.notFound"
    ),

    RESERVATION_CONFLICT(
            HttpStatus.CONFLICT,
            "reservation.conflict"
    ),

    INVALID_RESERVATION_TIME(
            HttpStatus.BAD_REQUEST,
            "reservation.invalidTime"
    ),

    RESERVATION_IN_PAST(
            HttpStatus.BAD_REQUEST,
            "reservation.inPast"
    ),

    INVALID_RESERVATION_STATUS(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "reservation.invalidStatus"
    ),

    RESERVATION_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "reservation.accessDenied"
    ),

    // Visitor
    VISITOR_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "visitor.notFound"
    ),

    VISITOR_ALREADY_CHECKED_IN(
            HttpStatus.CONFLICT,
            "visitor.alreadyCheckedIn"
    ),

    VISITOR_ALREADY_CHECKED_OUT(
            HttpStatus.CONFLICT,
            "visitor.alreadyCheckedOut"
    ),

    INVALID_VISITOR_STATUS(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "visitor.invalidStatus"
    ),

    INVALID_CHECKOUT_TIME(
            HttpStatus.BAD_REQUEST,
            "visitor.invalidCheckoutTime"
    ),

    // Visitor Card
    VISITOR_CARD_ALREADY_IN_USE(
            HttpStatus.CONFLICT,
            "visitorCard.alreadyInUse"
    ),

    // Security
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "security.forbidden"
    ),

    TENANT_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "tenant.accessDenied"
    );

    private final HttpStatus httpStatus;
    private final String messageKey;

    ErrorCode(HttpStatus httpStatus, String messageKey) {
        this.httpStatus = httpStatus;
        this.messageKey = messageKey;
    }
}