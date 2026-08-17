-- Department permissions were introduced after the HR template was seeded.
INSERT INTO role_template_permissions (role_template_id, permission_id)
SELECT rt.id, p.id
FROM role_templates rt
CROSS JOIN permissions p
WHERE rt.name = 'İnsan Kaynakları'
  AND p.code IN (
    'DEPARTMENT_VIEW', 'DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE',
    'DEPARTMENT_DELETE', 'DEPARTMENT_ACTIVATE',
    'DEPARTMENT_DEACTIVATE', 'DEPARTMENT_MANAGE'
  )
ON CONFLICT (role_template_id, permission_id) DO NOTHING;

-- Synchronize HR roles already created for existing companies with the template.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, rtp.permission_id
FROM roles r
JOIN role_templates rt ON rt.name = 'İnsan Kaynakları'
JOIN role_template_permissions rtp ON rtp.role_template_id = rt.id
WHERE r.name = 'İnsan Kaynakları'
ON CONFLICT (role_id, permission_id) DO NOTHING;
