CREATE TABLE notifications
(
    id                BIGSERIAL PRIMARY KEY,
    company_id        BIGINT        NOT NULL REFERENCES companies (id),
    recipient_user_id BIGINT        NOT NULL REFERENCES users (id),
    title             VARCHAR(200)  NOT NULL,
    message           VARCHAR(1000) NOT NULL,
    reservation_id    BIGINT,
    read              BOOLEAN       NOT NULL DEFAULT false,
    active            BOOLEAN       NOT NULL DEFAULT true,
    version           BIGINT,
    created_at        TIMESTAMP     NOT NULL,
    created_by        BIGINT,
    updated_at        TIMESTAMP     NOT NULL,
    updated_by        BIGINT,
    deactivated_at    TIMESTAMP
);

CREATE INDEX idx_notifications_recipient_user_id ON notifications (recipient_user_id);
CREATE INDEX idx_notifications_company_id ON notifications (company_id);