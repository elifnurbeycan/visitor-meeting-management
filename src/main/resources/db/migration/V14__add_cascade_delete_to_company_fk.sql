-- job_titles
ALTER TABLE job_titles DROP CONSTRAINT job_titles_company_id_fkey;
ALTER TABLE job_titles ADD CONSTRAINT job_titles_company_id_fkey
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- roles
ALTER TABLE roles DROP CONSTRAINT roles_company_id_fkey;
ALTER TABLE roles ADD CONSTRAINT roles_company_id_fkey
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- users
ALTER TABLE users DROP CONSTRAINT users_company_id_fkey;
ALTER TABLE users ADD CONSTRAINT users_company_id_fkey
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- user_permission_overrides
ALTER TABLE user_permission_overrides DROP CONSTRAINT user_permission_overrides_company_id_fkey;
ALTER TABLE user_permission_overrides ADD CONSTRAINT user_permission_overrides_company_id_fkey
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- features
ALTER TABLE features DROP CONSTRAINT features_company_id_fkey;
ALTER TABLE features ADD CONSTRAINT features_company_id_fkey
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- rooms
ALTER TABLE rooms DROP CONSTRAINT rooms_company_id_fkey;
ALTER TABLE rooms ADD CONSTRAINT rooms_company_id_fkey
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- reservations
ALTER TABLE reservations DROP CONSTRAINT reservations_company_id_fkey;
ALTER TABLE reservations ADD CONSTRAINT reservations_company_id_fkey
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;