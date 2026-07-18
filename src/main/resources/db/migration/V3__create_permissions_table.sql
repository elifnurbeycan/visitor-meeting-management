CREATE TABLE permissions
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    code                 VARCHAR(100) NOT NULL,
    name                 VARCHAR(150) NOT NULL,
    description          VARCHAR(500),
    category             VARCHAR(50)  NOT NULL,
    is_system_permission BOOLEAN      NOT NULL,
    display_order        INTEGER      NOT NULL,

    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT now(),
    created_by           BIGINT,
    updated_by           BIGINT,
    version              BIGINT       NOT NULL DEFAULT 0,
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at       TIMESTAMP,

    CONSTRAINT uk_permissions_code UNIQUE (code)
);

CREATE INDEX idx_permissions_category ON permissions (category);
CREATE INDEX idx_permissions_active ON permissions (active);