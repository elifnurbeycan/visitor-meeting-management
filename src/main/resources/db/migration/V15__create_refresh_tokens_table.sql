CREATE TABLE refresh_tokens
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    token_hash     VARCHAR(255) NOT NULL,

    user_id        BIGINT REFERENCES users (id) ON DELETE CASCADE,
    super_admin_id BIGINT REFERENCES super_admins (id) ON DELETE CASCADE,

    expires_at     TIMESTAMP    NOT NULL,
    revoked        BOOLEAN      NOT NULL DEFAULT FALSE,
    revoked_at     TIMESTAMP,

    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     BIGINT,
    updated_by     BIGINT,
    version        BIGINT       NOT NULL DEFAULT 0,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,

    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT chk_refresh_tokens_owner CHECK (
        (user_id IS NOT NULL AND super_admin_id IS NULL) OR
        (user_id IS NULL AND super_admin_id IS NOT NULL)
        )
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_super_admin_id ON refresh_tokens (super_admin_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);