ALTER TABLE rooms
    ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_rooms_company_archived ON rooms (company_id, archived);
