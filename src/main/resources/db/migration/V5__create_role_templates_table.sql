CREATE TABLE role_templates
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    name           VARCHAR(100) NOT NULL,
    description    VARCHAR(500),

    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     BIGINT,
    updated_by     BIGINT,
    version        BIGINT       NOT NULL DEFAULT 0,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,

    CONSTRAINT uk_role_templates_name UNIQUE (name)
);

CREATE INDEX idx_role_templates_active ON role_templates (active);


CREATE TABLE role_template_permissions
(
    role_template_id BIGINT NOT NULL REFERENCES role_templates (id),
    permission_id    BIGINT NOT NULL REFERENCES permissions (id),

    CONSTRAINT uk_role_template_permissions_template_permission
        UNIQUE (role_template_id, permission_id)
);

CREATE INDEX idx_role_template_permissions_template_id ON role_template_permissions (role_template_id);
CREATE INDEX idx_role_template_permissions_permission_id ON role_template_permissions (permission_id);