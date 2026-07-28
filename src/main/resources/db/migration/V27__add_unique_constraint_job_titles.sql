ALTER TABLE job_titles
    ADD CONSTRAINT uk_job_titles_company_name UNIQUE (company_id, name);