# GH Transport - 物流ERP系统

基于领域驱动设计(DDD)的物流ERP系统，采用四层架构 + CQRS + 事件驱动架构。

## 技术栈

- **语言**: Java 17
- **框架**: Spring Boot 3.2
- **数据库**: PostgreSQL, MongoDB
- **ORM**: MyBatis-Plus, Spring Data MongoDB
- **中间件**: Kafka, Redis
- **服务发现**: Nacos
- **流量防护**: Sentinel
- **API文档**: Knife4j
- **构建工具**: Maven

## 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                      接口层 (Interfaces)                     │
│                   Controller / REST API                      │
├─────────────────────────────────────────────────────────────┤
│                      应用层 (Application)                    │
│                 Application Service / Assembler              │
├─────────────────────────────────────────────────────────────┤
│                       领域层 (Domain)                        │
│            Aggregate / Entity / ValueObject / Event          │
├─────────────────────────────────────────────────────────────┤
│                   基础设施层 (Infrastructure)                │
│               Repository / Mapper / Event Publisher          │
└─────────────────────────────────────────────────────────────┘
```

## 项目结构

```
gh-transport-parent/
├── gh-transport-common/              # 共享领域基类
├── gh-transport-gateway/             # API网关
├── gh-transport-auth/                # 认证授权
├── gh-transport-customer/            # 客户上下文
│   ├── customer-domain/              # 纯Java，无Spring依赖
│   ├── customer-application/
│   ├── customer-infrastructure/
│   └── customer-interfaces/
├── gh-transport-order/               # 核心域 - 订单管理
│   ├── order-domain/
│   ├── order-application/
│   ├── order-infrastructure/
│   └── order-interfaces/
├── gh-transport-dispatch/            # 调度上下文
├── gh-transport-transport/           # 运输上下文
├── gh-transport-inventory/           # 库存上下文
└── gh-transport-system/              # 系统上下文
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- PostgreSQL 14+
- MongoDB 6+
- Redis 7+
- Nacos 2.x (可选)

### 编译运行

```bash
# 编译整个项目
mvn clean install -DskipTests

# 运行订单服务
cd gh-transport-order/order-interfaces
mvn spring-boot:run
```

### API文档

启动服务后访问: http://localhost:8080/doc.html

## 模块说明

### 订单模块 (gh-transport-order)

提供完整的订单管理功能：

- 创建订单
- 取消订单
- 订单状态流转 (待调度 → 已调度 → 运输中 → 已送达 → 已完成)
- 订单查询

### 客户模块 (gh-transport-customer)

客户信息管理

### 调度模块 (gh-transport-dispatch)

订单调度管理

### 运输模块 (gh-transport-transport)

运输过程管理

## 设计文档

- [架构设计](docs/plans/2025-01-27-logistics-erp-ddd-design.md)
- [实施计划](docs/plans/2025-01-27-logistics-erp-ddd-implementation.md)

## License

MIT
