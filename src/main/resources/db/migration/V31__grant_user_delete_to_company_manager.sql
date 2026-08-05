-- Allow delegated company managers to permanently delete users when domain rules permit it.
INSERT INTO role_template_permissions (role_template_id, permission_id)
SELECT rt.id, p.id
FROM role_templates rt
JOIN permissions p ON p.code = 'USER_DELETE'
WHERE rt.name = 'Şirket Yöneticisi'
ON CONFLICT (role_template_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'USER_DELETE'
WHERE r.name = 'Şirket Yöneticisi'
ON CONFLICT (role_id, permission_id) DO NOTHING;
