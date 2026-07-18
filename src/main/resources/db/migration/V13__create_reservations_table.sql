CREATE TABLE reservations
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    company_id        BIGINT       NOT NULL REFERENCES companies (id),
    room_id           BIGINT       NOT NULL,
    organizer_id      BIGINT       NOT NULL,

    title             VARCHAR(200) NOT NULL,
    description       VARCHAR(1000),
    start_time        TIMESTAMP    NOT NULL,
    end_time          TIMESTAMP    NOT NULL,
    participant_count INTEGER      NOT NULL,

    status            VARCHAR(30)  NOT NULL DEFAULT 'PENDING_APPROVAL',
    approval_deadline TIMESTAMP,
    rejection_reason  VARCHAR(500),
    cancel_reason     VARCHAR(500),
    cancelled_at      TIMESTAMP,
    completed_at      TIMESTAMP,

    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now(),
    created_by        BIGINT,
    updated_by        BIGINT,
    version           BIGINT       NOT NULL DEFAULT 0,
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at    TIMESTAMP,

    CONSTRAINT fk_reservations_room
        FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_reservations_organizer
        FOREIGN KEY (organizer_id) REFERENCES users (id)
);

CREATE INDEX idx_reservations_company_id ON reservations (company_id);
CREATE INDEX idx_reservations_room_id ON reservations (room_id);
CREATE INDEX idx_reservations_organizer_id ON reservations (organizer_id);
CREATE INDEX idx_reservations_status ON reservations (status);
CREATE INDEX idx_reservations_room_time ON reservations (room_id, start_time, end_time);
CREATE INDEX idx_reservations_approval_deadline ON reservations (approval_deadline);
CREATE INDEX idx_reservations_active ON reservations (active);