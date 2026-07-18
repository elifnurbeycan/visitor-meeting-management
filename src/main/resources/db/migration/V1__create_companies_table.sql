CREATE TABLE companies
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    slug             VARCHAR(100) NOT NULL,
    description      VARCHAR(1000),
    tax_number       VARCHAR(20),
    contact_email    VARCHAR(150) NOT NULL,
    contact_phone    VARCHAR(20),
    address          VARCHAR(500),
    industry         VARCHAR(100),

    status           VARCHAR(30)  NOT NULL DEFAULT 'PENDING_APPROVAL',
    rejection_reason VARCHAR(500),

    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now(),
    created_by       BIGINT,
    updated_by       BIGINT,
    version          BIGINT       NOT NULL DEFAULT 0,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at   TIMESTAMP,

    CONSTRAINT uk_companies_slug UNIQUE (slug),
    CONSTRAINT uk_companies_tax_number UNIQUE (tax_number)
);

CREATE INDEX idx_companies_active ON companies (active);
CREATE INDEX idx_companies_status ON companies (status);