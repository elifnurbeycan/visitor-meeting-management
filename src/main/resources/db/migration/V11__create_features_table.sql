CREATE TABLE features
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
    deactivated_at TIMESTAMP,

    CONSTRAINT uk_features_company_name UNIQUE (company_id, name)
);

CREATE INDEX idx_features_company_id ON features (company_id);
CREATE INDEX idx_features_active ON features (active);