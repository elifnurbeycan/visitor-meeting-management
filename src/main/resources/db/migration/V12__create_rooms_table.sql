CREATE TABLE rooms
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    company_id     BIGINT       NOT NULL REFERENCES companies (id),

    name           VARCHAR(150) NOT NULL,
    location       VARCHAR(150),
    capacity       INTEGER      NOT NULL,
    description    VARCHAR(1000),

    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     BIGINT,
    updated_by     BIGINT,
    version        BIGINT       NOT NULL DEFAULT 0,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP,

    CONSTRAINT uk_rooms_company_name UNIQUE (company_id, name)
);

CREATE INDEX idx_rooms_company_id ON rooms (company_id);
CREATE INDEX idx_rooms_active ON rooms (active);


CREATE TABLE room_features
(
    room_id    BIGINT NOT NULL REFERENCES rooms (id),
    feature_id BIGINT NOT NULL REFERENCES features (id),

    CONSTRAINT uk_room_features_room_feature
        UNIQUE (room_id, feature_id)
);

CREATE INDEX idx_room_features_room_id ON room_features (room_id);
CREATE INDEX idx_room_features_feature_id ON room_features (feature_id);