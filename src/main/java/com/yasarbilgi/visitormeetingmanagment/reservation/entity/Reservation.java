package com.yasarbilgi.visitormeetingmanagment.reservation.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.reservation.enums.ReservationStatus;
import com.yasarbilgi.visitormeetingmanagment.room.entity.Room;
import com.yasarbilgi.visitormeetingmanagment.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "idx_reservations_company_id", columnList = "company_id"),
                @Index(name = "idx_reservations_room_id", columnList = "room_id"),
                @Index(name = "idx_reservations_organizer_id", columnList = "organizer_id"),
                @Index(name = "idx_reservations_status", columnList = "status"),
                @Index(name = "idx_reservations_room_time", columnList = "room_id, start_time, end_time"),
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

    @Column(name = "participant_count", nullable = false)
    private Integer participantCount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status = ReservationStatus.ACTIVE;

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

    public void updateDetails(
            String newTitle,
            String newDescription,
            LocalDateTime newStartTime,
            LocalDateTime newEndTime,
            Integer newParticipantCount,
            Room newRoom
    ) {
        validateEditableStatus();
        validateTitle(newTitle);
        validateTimeRange(newStartTime, newEndTime);
        validateParticipantCount(newParticipantCount);
        validateCapacity(newParticipantCount, newRoom);

        this.title = newTitle;
        this.description = newDescription;
        this.startTime = newStartTime;
        this.endTime = newEndTime;
        this.participantCount = newParticipantCount;
        this.room = newRoom;
    }

    public void cancel(String reason) {
        validateEditableStatus();

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

    private void validateEditableStatus() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
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

    private static void validateParticipantCount(Integer participantCount) {
        if (participantCount == null || participantCount < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARTICIPANT_COUNT);
        }
    }

    private static void validateCapacity(Integer participantCount, Room newRoom) {
        if (newRoom != null && participantCount != null && participantCount > newRoom.getCapacity()) {
            throw new BusinessException(ErrorCode.RESERVATION_EXCEEDS_ROOM_CAPACITY);
        }
    }
}