package com.yasarbilgi.visitormeetingmanagment.reservation.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
@Entity
@Filter(name = "tenantFilter")
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "idx_reservations_company_id", columnList = "company_id"),
                @Index(name = "idx_reservations_room_id", columnList = "room_id"),
                @Index(name = "idx_reservations_organizer_id", columnList = "organizer_id"),
                @Index(name = "idx_reservations_status", columnList = "status"),
                @Index(name = "idx_reservations_room_time", columnList = "room_id, start_time, end_time"),
                @Index(name = "idx_reservations_approval_deadline", columnList = "approval_deadline"),
                @Index(name = "idx_reservations_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends TenantBaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status = ReservationStatus.PENDING_APPROVAL;

    @Column(name = "approval_deadline")
    private Instant approvalDeadline;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservations_room"))
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservations_organizer"))
    private User organizer;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "reservation_participants",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    public void updateDetails(
            String newTitle,
            String newDescription,
            LocalDateTime newStartTime,
            LocalDateTime newEndTime,
            Room newRoom
    ) {
        validateEditableStatus();
        validateTitle(newTitle);
        validateTimeRange(newStartTime, newEndTime);

        this.title = newTitle;
        this.description = newDescription;
        this.startTime = newStartTime;
        this.endTime = newEndTime;
        this.room = newRoom;
    }

    public void approve() {
        validatePendingStatus();
        this.status = ReservationStatus.ACTIVE;
        this.approvalDeadline = null;
    }

    public void reject(String reason) {
        validatePendingStatus();
        this.status = ReservationStatus.REJECTED;
        this.rejectionReason = reason;
        this.approvalDeadline = null;
    }

    public void autoRejectDueToConflict() {
        validatePendingStatus();
        this.status = ReservationStatus.REJECTED;
        this.rejectionReason = null; // servis katmanında sabit bir sistem mesajı ile doldurulacak
        this.approvalDeadline = null;
    }

    public void expire() {
        validatePendingStatus();
        this.status = ReservationStatus.EXPIRED;
        this.approvalDeadline = null;
    }

    public void cancel(String reason) {
        validateCancellableStatus();

        this.status = ReservationStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = Instant.now();
    }

    public void complete() {
        validateEditableStatus();

        this.status = ReservationStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public boolean belongsTo(User user) {
        return this.organizer != null
                && user != null
                && this.organizer.equals(user);
    }

    public boolean isApprovalExpired() {
        return this.status == ReservationStatus.PENDING_APPROVAL
                && this.approvalDeadline != null
                && Instant.now().isAfter(this.approvalDeadline);
    }

    private void validateEditableStatus() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }
    }

    private void validatePendingStatus() {
        if (this.status != ReservationStatus.PENDING_APPROVAL) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_PENDING_APPROVAL);
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.RESERVATION_TITLE_REQUIRED);
        }
    }

    private static void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RESERVATION_IN_PAST);
        }
    }

    private void validateCancellableStatus() {
        if (this.status != ReservationStatus.ACTIVE && this.status != ReservationStatus.PENDING_APPROVAL) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }
    }

    public void addParticipant(User user) {
        this.participants.add(user);
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    public boolean hasParticipant(User user) {
        return this.participants.contains(user);
    }

    public boolean exceedsRoomCapacity() {
        return this.participants.size() > this.room.getCapacity();
    }
}