-- =====================================================
-- GH Transport 数据库DDL脚本
-- Database: PostgreSQL 15+
-- =====================================================

-- =====================================================
-- 认证服务 (gh_transport_auth)
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS auth_user (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    mobile VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(128),
    nickname VARCHAR(32) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    roles JSONB DEFAULT '[]',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 用户索引
CREATE INDEX IF NOT EXISTS idx_auth_user_username ON auth_user(username);
CREATE INDEX IF NOT EXISTS idx_auth_user_mobile ON auth_user(mobile);
CREATE INDEX IF NOT EXISTS idx_auth_user_status ON auth_user(status);

-- 角色表
CREATE TABLE IF NOT EXISTS auth_role (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    permissions JSONB DEFAULT '[]',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 角色索引
CREATE INDEX IF NOT EXISTS idx_auth_role_code ON auth_role(code);
CREATE INDEX IF NOT EXISTS idx_auth_role_status ON auth_role(status);

-- =====================================================
-- 订单服务 (gh_transport_order)
-- =====================================================

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id VARCHAR(36) PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    shipping_address JSONB NOT NULL,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 订单索引
CREATE INDEX IF NOT EXISTS idx_order_customer_id ON t_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON t_order(status);
CREATE INDEX IF NOT EXISTS idx_order_created_at ON t_order(created_at);

-- 订单明细表
CREATE TABLE IF NOT EXISTS t_order_item (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL REFERENCES t_order(id) ON DELETE CASCADE,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    specification VARCHAR(64),
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL
);

-- 订单明细索引
CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON t_order_item(order_id);

-- =====================================================
-- 调度服务 (gh_transport_dispatch)
-- =====================================================

-- 调度单表
CREATE TABLE IF NOT EXISTS t_dispatch (
    id VARCHAR(36) PRIMARY KEY,
    dispatch_no VARCHAR(32) NOT NULL UNIQUE,
    order_id VARCHAR(36) NOT NULL,
    vehicle_id VARCHAR(36),
    driver_id VARCHAR(36),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    planned_time TIMESTAMP,
    actual_time TIMESTAMP,
    origin_address VARCHAR(500) NOT NULL,
    destination_address VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 调度单索引
CREATE INDEX IF NOT EXISTS idx_dispatch_order_id ON t_dispatch(order_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_vehicle_id ON t_dispatch(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_driver_id ON t_dispatch(driver_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_status ON t_dispatch(status);

-- 车辆表
CREATE TABLE IF NOT EXISTS t_vehicle (
    id VARCHAR(36) PRIMARY KEY,
    plate_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type VARCHAR(32) NOT NULL,
    capacity DECIMAL(10, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    driver_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 司机表
CREATE TABLE IF NOT EXISTS t_driver (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    license_no VARCHAR(32),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 客户服务 (gh_transport_customer)
-- =====================================================

-- 客户表
CREATE TABLE IF NOT EXISTS t_customer (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    contact_person VARCHAR(64),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(128),
    address VARCHAR(500),
    type VARCHAR(20) NOT NULL DEFAULT 'ENTERPRISE',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 客户索引
CREATE INDEX IF NOT EXISTS idx_customer_type ON t_customer(type);
CREATE INDEX IF NOT EXISTS idx_customer_status ON t_customer(status);

-- =====================================================
-- 库存服务 (gh_transport_inventory)
-- =====================================================

-- 库存表
CREATE TABLE IF NOT EXISTS t_inventory (
    id VARCHAR(36) PRIMARY KEY,
    sku_code VARCHAR(64) NOT NULL UNIQUE,
    product_name VARCHAR(128) NOT NULL,
    warehouse_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(10, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 库存索引
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse ON t_inventory(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventory_status ON t_inventory(status);

-- 仓库表
CREATE TABLE IF NOT EXISTS t_warehouse (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(500) NOT NULL,
    contact_person VARCHAR(64),
    contact_phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 公共表
-- =====================================================

-- 领域事件表（用于本地事件表模式）
CREATE TABLE IF NOT EXISTS domain_event (
    id VARCHAR(64) PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    event_payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    retry_count INT NOT NULL DEFAULT 0
);

-- 领域事件索引
CREATE INDEX IF NOT EXISTS idx_domain_event_status ON domain_event(status);
CREATE INDEX IF NOT EXISTS idx_domain_event_aggregate ON domain_event(aggregate_type, aggregate_id);

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id VARCHAR(36) PRIMARY KEY,
    operator_id VARCHAR(36),
    operator_type VARCHAR(20),
    operation_type VARCHAR(32) NOT NULL,
    resource_type VARCHAR(64),
    resource_id VARCHAR(64),
    description VARCHAR(500),
    request_data TEXT,
    response_data TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志索引
CREATE INDEX IF NOT EXISTS idx_op_log_operator ON operation_log(operator_id);
CREATE INDEX IF NOT EXISTS idx_op_log_resource ON operation_log(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_op_log_created ON operation_log(created_at);
