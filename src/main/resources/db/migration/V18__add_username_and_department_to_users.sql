ALTER TABLE users ADD COLUMN username VARCHAR(50);
ALTER TABLE users ADD COLUMN department_id BIGINT REFERENCES departments(id);

-- Mevcut kayıtlar varsa (test verisi), username boş kalmasın diye geçici bir değer atanabilir.
-- Gerçek üretimde bu satır gerekmeyebilir, sadece dev/test ortamı için güvenlik.
UPDATE users SET username = CONCAT('user_', id) WHERE username IS NULL;

ALTER TABLE users ALTER COLUMN username SET NOT NULL;

CREATE UNIQUE INDEX uk_users_username ON users (username);
CREATE INDEX idx_users_department_id ON users (department_id);