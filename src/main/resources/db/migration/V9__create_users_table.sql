CREATE TABLE users
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    company_id     BIGINT       NOT NULL REFERENCES companies (id),
    job_title_id   BIGINT REFERENCES job_titles (id),

    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    is_owner       BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     BIGINT,
    updated_by     BIGINT,
    version        BIGINT       NOT NULL DEFAULT 0,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,

    CONSTRAINT uk_users_company_email UNIQUE (company_id, email)
);

CREATE INDEX idx_users_company_id ON users (company_id);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_job_title_id ON users (job_title_id);

-- Bir şirkette en fazla 1 owner olabilir (partial unique index).
-- Daha önce konuştuğumuz "tek owner" kuralının veritabanı seviyesindeki güvenlik ağı.
CREATE UNIQUE INDEX uk_users_company_owner
    ON users (company_id) WHERE is_owner = TRUE;


CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL REFERENCES users (id),
    role_id BIGINT NOT NULL REFERENCES roles (id),

    CONSTRAINT uk_user_roles_user_role
        UNIQUE (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);