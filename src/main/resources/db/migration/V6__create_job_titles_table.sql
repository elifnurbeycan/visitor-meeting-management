CREATE TABLE job_titles
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    company_id     BIGINT       NOT NULL REFERENCES companies (id),

    name           VARCHAR(100) NOT NULL,
    description    VARCHAR(500),

    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     BIGINT,
    updated_by     BIGINT,
    version        BIGINT       NOT NULL DEFAULT 0,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP
);

CREATE INDEX idx_job_titles_name ON job_titles (name);
CREATE INDEX idx_job_titles_active ON job_titles (active);
CREATE INDEX idx_job_titles_company_id ON job_titles (company_id);