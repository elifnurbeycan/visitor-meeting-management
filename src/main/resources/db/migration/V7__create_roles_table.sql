CREATE TABLE roles
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    company_id     BIGINT       NOT NULL REFERENCES companies (id),

    name           VARCHAR(100) NOT NULL,
    description    VARCHAR(500),
    is_system_role BOOLEAN      NOT NULL,

    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     BIGINT,
    updated_by     BIGINT,
    version        BIGINT       NOT NULL DEFAULT 0,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,

    CONSTRAINT uk_roles_company_name UNIQUE (company_id, name)
);

CREATE INDEX idx_roles_company_id ON roles (company_id);


CREATE TABLE role_permissions
(
    role_id       BIGINT NOT NULL REFERENCES roles (id),
    permission_id BIGINT NOT NULL REFERENCES permissions (id),

    CONSTRAINT uk_role_permissions_role_permission
        UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions (permission_id);