CREATE TABLE job_title_default_roles
(
    job_title_id BIGINT NOT NULL REFERENCES job_titles (id),
    role_id      BIGINT NOT NULL REFERENCES roles (id),

    CONSTRAINT uk_job_title_default_roles
        UNIQUE (job_title_id, role_id)
);

CREATE INDEX idx_job_title_default_roles_job_title_id ON job_title_default_roles (job_title_id);
CREATE INDEX idx_job_title_default_roles_role_id ON job_title_default_roles (role_id);