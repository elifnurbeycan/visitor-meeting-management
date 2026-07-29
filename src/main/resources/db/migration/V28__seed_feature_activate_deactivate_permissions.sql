INSERT INTO permissions (code, name, description, category, is_system_permission, display_order, created_at, updated_at,
                         version, active)
VALUES ('FEATURE_ACTIVATE', 'Özellik Aktifleştirme', 'Oda özelliğini aktif hale getirme yetkisi', 'FEATURE_MANAGEMENT',
        true, 160, now(), now(), 0, true),
       ('FEATURE_DEACTIVATE', 'Özellik Pasifleştirme', 'Oda özelliğini pasif hale getirme yetkisi',
        'FEATURE_MANAGEMENT', true, 161, now(), now(), 0, true);