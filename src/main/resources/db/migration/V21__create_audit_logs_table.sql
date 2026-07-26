CREATE TABLE audit_logs
(
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT       NOT NULL REFERENCES companies (id),
    actor_user_id  BIGINT,
    action         VARCHAR(100) NOT NULL,
    target_type    VARCHAR(50)  NOT NULL,
    target_id      BIGINT,
    details        VARCHAR(1000),
    active         BOOLEAN      NOT NULL DEFAULT true,
    version        BIGINT,
    created_at     TIMESTAMP    NOT NULL,
    created_by     BIGINT,
    updated_at     TIMESTAMP    NOT NULL,
    updated_by     BIGINT,
    deactivated_at TIMESTAMP
);

CREATE INDEX idx_audit_logs_company_id ON audit_logs (company_id);
CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs (actor_user_id);
CREATE INDEX idx_audit_logs_target ON audit_logs (target_type, target_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);