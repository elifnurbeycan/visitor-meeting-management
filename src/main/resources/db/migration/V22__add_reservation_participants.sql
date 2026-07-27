ALTER TABLE reservations DROP COLUMN participant_count;

CREATE TABLE reservation_participants
(
    reservation_id BIGINT NOT NULL REFERENCES reservations (id),
    user_id        BIGINT NOT NULL REFERENCES users (id),
    PRIMARY KEY (reservation_id, user_id)
);

CREATE INDEX idx_reservation_participants_user_id ON reservation_participants (user_id);