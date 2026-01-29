-- =====================================================
-- GH Transport 初始化数据
-- =====================================================

-- =====================================================
-- 认证服务初始化数据
-- =====================================================

-- 初始化角色
INSERT INTO auth_role (id, code, name, description, permissions, status, system_role)
VALUES
('role-001', 'ADMIN', '系统管理员', '系统最高权限管理员', '["*"]'::jsonb, 'ENABLED', TRUE),
('role-002', 'OPERATOR', '运营人员', '负责日常运营管理', '["order:*", "dispatch:*", "customer:*"]'::jsonb, 'ENABLED', FALSE),
('role-003', 'DRIVER', '司机', '负责运输和配送', '["transport:*"]'::jsonb, 'ENABLED', FALSE),
('role-004', 'VIEWER', '查看人员', '只读权限', '["order:read", "dispatch:read", "customer:read"]'::jsonb, 'ENABLED', FALSE)
ON CONFLICT (code) DO NOTHING;

-- 初始化管理员用户 (密码: admin123)
INSERT INTO auth_user (id, username, password, mobile, email, nickname, status, roles)
VALUES
('user-001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqR/v2Y.BV6v6W6Q5tH3E1rK5Q5a5W', '13800000000', 'admin@ghtransport.com', '系统管理员', 'ACTIVE', '["role-001"]'::jsonb)
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- 客户服务初始化数据
-- =====================================================

INSERT INTO t_warehouse (id, name, address, contact_person, contact_phone, status)
VALUES
('wh-001', '北京总仓', '北京市朝阳区xxx', '张三', '13800000001', 'ACTIVE'),
('wh-002', '上海分仓', '上海市浦东新区xxx', '李四', '13800000002', 'ACTIVE'),
('wh-003', '广州分仓', '广州市天河区xxx', '王五', '13800000003', 'ACTIVE')
ON CONFLICT DO NOTHING;

INSERT INTO t_customer (id, name, contact_person, phone, email, address, type, status)
VALUES
('cust-001', '某某电商公司', '赵先生', '13900000001', 'zhao@example.com', '北京市海淀区xxx', 'ENTERPRISE', 'ACTIVE'),
('cust-002', '某某物流公司', '钱女士', '13900000002', 'qian@example.com', '上海市静安区xxx', 'ENTERPRISE', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 库存服务初始化数据
-- =====================================================

INSERT INTO t_inventory (id, sku_code, product_name, warehouse_id, quantity, reserved_quantity, unit_price, status)
VALUES
('inv-001', 'SKU-001', '商品A', 'wh-001', 1000, 0, 99.99, 'AVAILABLE'),
('inv-002', 'SKU-002', '商品B', 'wh-001', 500, 50, 199.99, 'AVAILABLE'),
('inv-003', 'SKU-003', '商品C', 'wh-002', 2000, 0, 49.99, 'AVAILABLE')
ON CONFLICT (sku_code) DO NOTHING;

-- =====================================================
-- 调度服务初始化数据
-- =====================================================

INSERT INTO t_driver (id, name, phone, license_no, status)
VALUES
('driver-001', '司机A', '13900000010', 'LIC-001', 'AVAILABLE'),
('driver-002', '司机B', '13900000011', 'LIC-002', 'AVAILABLE')
ON CONFLICT DO NOTHING;

INSERT INTO t_vehicle (id, plate_number, vehicle_type, capacity, status, driver_id)
VALUES
('veh-001', '京A12345', '货车', 5000, 'AVAILABLE', 'driver-001'),
('veh-002', '京A67890', '货车', 10000, 'AVAILABLE', 'driver-002')
ON CONFLICT (plate_number) DO NOTHING;
