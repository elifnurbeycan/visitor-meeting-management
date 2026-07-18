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

    COMPANY_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "company.nameRequired"
    ),

    COMPANY_INVALID_SLUG(
            HttpStatus.BAD_REQUEST,
            "company.invalidSlug"
    ),

    COMPANY_EMAIL_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "company.emailRequired"
    ),

    COMPANY_NOT_PENDING_APPROVAL(
            HttpStatus.CONFLICT,
            "company.notPendingApproval"
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

    ROLE_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "role.nameRequired"
    ),

    ROLE_SYSTEM_ROLE_CANNOT_BE_DEACTIVATED(
            HttpStatus.FORBIDDEN,
            "role.systemRoleCannotBeDeactivated"
    ),

    ROLE_TEMPLATE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "roleTemplate.notFound"
    ),

    ROLE_TEMPLATE_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "roleTemplate.nameRequired"
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

    PERMISSION_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "permission.nameRequired"
    ),

    SYSTEM_PERMISSION_CANNOT_BE_DEACTIVATED(
            HttpStatus.FORBIDDEN,
            "permission.systemPermissionCannotBeDeactivated"
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

    JOB_TITLE_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "jobTitle.nameRequired"
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

    USER_OWNER_ROLE_MODIFICATION_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "user.ownerRoleModificationForbidden"
    ),

    USER_OWNER_CANNOT_BE_DEACTIVATED(
            HttpStatus.FORBIDDEN,
            "user.ownerCannotBeDeactivated"
    ),

    // Feature
    FEATURE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "feature.notFound"
    ),

    FEATURE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "feature.alreadyExists"
    ),

    FEATURE_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "feature.nameRequired"
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

    MEETING_ROOM_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "meetingRoom.nameRequired"
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
    ),

    // Authorization / Owner-Admin Rules
    LAST_ADMIN_CANNOT_BE_MODIFIED(
            HttpStatus.CONFLICT,
            "authorization.lastAdminCannotBeModified"
    ),

    SELF_MODIFICATION_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "authorization.selfModificationForbidden"
    ),

    ADMIN_CANNOT_MODIFY_ANOTHER_ADMIN(
            HttpStatus.FORBIDDEN,
            "authorization.adminCannotModifyAnotherAdmin"
    ),

    OWNER_TRANSFER_REQUIRED(
            HttpStatus.CONFLICT,
            "authorization.ownerTransferRequired"
    ),

    INSUFFICIENT_PERMISSION(
            HttpStatus.FORBIDDEN,
            "authorization.insufficientPermission"
    ),

    DUPLICATE_PERMISSION_OVERRIDE(
            HttpStatus.CONFLICT,
            "userPermissionOverride.duplicate"
    ),

    PERMISSION_OVERRIDE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "userPermissionOverride.notFound"
    ),

    // Generic / Fallback
    VALIDATION_FAILED(
            HttpStatus.BAD_REQUEST,
            "common.validationFailed"
    ),

    RESERVATION_TITLE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "reservation.titleRequired"
    ),

    INVALID_PARTICIPANT_COUNT(
            HttpStatus.BAD_REQUEST,
            "reservation.invalidParticipantCount"
    ),

    SUPER_ADMIN_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "superAdmin.notFound"
    ),

    SUPER_ADMIN_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "superAdmin.nameRequired"
    ),

    SUPER_ADMIN_PASSWORD_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "superAdmin.passwordRequired"
    ),

    SUPER_ADMIN_NOT_ACTIVE(
            HttpStatus.FORBIDDEN,
            "superAdmin.notActive"
    ),

    RESERVATION_EXCEEDS_ROOM_CAPACITY(
            HttpStatus.BAD_REQUEST,
            "reservation.exceedsRoomCapacity"
    ),

    RESERVATION_NOT_PENDING_APPROVAL(
            HttpStatus.CONFLICT,
            "reservation.notPendingApproval"
    ),

    RESERVATION_ALREADY_MODIFIED(
            HttpStatus.CONFLICT,
            "reservation.alreadyModified"
    ),

    RESERVATION_APPROVAL_EXPIRED(
            HttpStatus.CONFLICT,
            "reservation.approvalExpired"
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "common.internalServerError"
    );



    private final HttpStatus httpStatus;
    private final String messageKey;

    ErrorCode(HttpStatus httpStatus, String messageKey) {
        this.httpStatus = httpStatus;
        this.messageKey = messageKey;
    }
}