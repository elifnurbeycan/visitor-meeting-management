CREATE TABLE user_permission_overrides
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    company_id     BIGINT      NOT NULL REFERENCES companies (id),
    user_id        BIGINT      NOT NULL REFERENCES users (id),
    permission_id  BIGINT      NOT NULL REFERENCES permissions (id),

    type           VARCHAR(20) NOT NULL,

    created_at     TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT now(),
    created_by     BIGINT,
    updated_by     BIGINT,
    version        BIGINT      NOT NULL DEFAULT 0,
    active         BOOLEAN     NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,

    CONSTRAINT uk_user_permission_overrides_user_permission
        UNIQUE (user_id, permission_id)
);

CREATE INDEX idx_user_permission_overrides_user_id ON user_permission_overrides (user_id);
CREATE INDEX idx_user_permission_overrides_permission_id ON user_permission_overrides (permission_id);
CREATE INDEX idx_user_permission_overrides_company_id ON user_permission_overrides (company_id);