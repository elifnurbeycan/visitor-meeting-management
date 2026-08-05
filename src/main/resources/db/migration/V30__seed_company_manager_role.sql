-- Delegated tenant administration role. The company owner remains the only owner;
-- ownership transfer and per-user permission override authorities are intentionally excluded.
INSERT INTO role_templates (name, description, created_at, updated_at, version, active)
VALUES ('Şirket Yöneticisi',
        'Şirket sahibinin sahipliği devretmeden yönetim işlemlerini yetkilendirebildiği rol',
        now(), now(), 0, true)
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_template_permissions (role_template_id, permission_id)
SELECT rt.id, p.id
FROM role_templates rt
CROSS JOIN permissions p
WHERE rt.name = 'Şirket Yöneticisi'
  AND p.code IN (
    'COMPANY_VIEW', 'COMPANY_UPDATE', 'COMPANY_MANAGE_SETTINGS',
    'USER_CREATE', 'USER_VIEW', 'USER_VIEW_ALL', 'USER_UPDATE', 'USER_DEACTIVATE', 'USER_ACTIVATE',
    'USER_MANAGE', 'USER_ASSIGN_ROLE', 'USER_REVOKE_ROLE', 'USER_ASSIGN_JOB_TITLE',
    'ROLE_CREATE', 'ROLE_VIEW', 'ROLE_UPDATE', 'ROLE_DEACTIVATE', 'ROLE_ACTIVATE', 'ROLE_MANAGE',
    'ROLE_ASSIGN_PERMISSION', 'ROLE_REVOKE_PERMISSION', 'PERMISSION_VIEW',
    'JOB_TITLE_CREATE', 'JOB_TITLE_VIEW', 'JOB_TITLE_UPDATE', 'JOB_TITLE_DEACTIVATE',
    'JOB_TITLE_ACTIVATE', 'JOB_TITLE_MANAGE', 'JOB_TITLE_ASSIGN_DEFAULT_ROLE', 'JOB_TITLE_REMOVE_DEFAULT_ROLE',
    'DEPARTMENT_VIEW', 'DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE', 'DEPARTMENT_ACTIVATE',
    'DEPARTMENT_DEACTIVATE', 'DEPARTMENT_MANAGE',
    'ROOM_CREATE', 'ROOM_VIEW', 'ROOM_VIEW_AVAILABILITY', 'ROOM_UPDATE', 'ROOM_DEACTIVATE',
    'ROOM_ACTIVATE', 'ROOM_MANAGE_FEATURES',
    'FEATURE_CREATE', 'FEATURE_VIEW', 'FEATURE_UPDATE', 'FEATURE_ACTIVATE', 'FEATURE_DEACTIVATE',
    'RESERVATION_CREATE', 'RESERVATION_VIEW_OWN', 'RESERVATION_VIEW_ALL', 'RESERVATION_UPDATE_OWN',
    'RESERVATION_UPDATE_ALL', 'RESERVATION_CANCEL_OWN', 'RESERVATION_CANCEL_ALL', 'RESERVATION_APPROVE',
    'RESERVATION_REJECT', 'RESERVATION_VIEW_DETAILS', 'RESERVATION_FILTER_BY_DATE', 'RESERVATION_FILTER_BY_ROOM',
    'VISITOR_CREATE', 'VISITOR_VIEW', 'VISITOR_CHECK_IN', 'VISITOR_CHECK_OUT', 'VISITOR_ASSIGN_CARD',
    'VISITOR_VIEW_HISTORY', 'VISITOR_CANCEL', 'DASHBOARD_VIEW',
    'REPORT_VIEW_DAILY_VISITORS', 'REPORT_VIEW_ACTIVE_VISITORS', 'REPORT_VIEW_UNCHECKED_OUT_VISITORS',
    'REPORT_VIEW_ROOM_USAGE', 'REPORT_VIEW_RESERVATION_STATS', 'REPORT_VIEW_CANCELLATION_STATS',
    'REPORT_EXPORT_EXCEL', 'AUDIT_LOG_VIEW', 'NOTIFICATION_VIEW', 'NOTIFICATION_MANAGE_SETTINGS'
  )
ON CONFLICT (role_template_id, permission_id) DO NOTHING;

-- Approved companies created before this migration also receive the role.
INSERT INTO roles (company_id, name, description, created_at, updated_at, version, active)
SELECT c.id,
       'Şirket Yöneticisi',
       'Şirket sahibinin sahipliği devretmeden yönetim işlemlerini yetkilendirebildiği rol',
       now(), now(), 0, true
FROM companies c
WHERE c.status = 'ACTIVE'
ON CONFLICT (company_id, name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, rtp.permission_id
FROM roles r
JOIN role_templates rt ON rt.name = 'Şirket Yöneticisi'
JOIN role_template_permissions rtp ON rtp.role_template_id = rt.id
WHERE r.name = 'Şirket Yöneticisi'
ON CONFLICT (role_id, permission_id) DO NOTHING;
