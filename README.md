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
- **安全**: Spring Security + JWT
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
│   ├── common-core/                  # 核心工具类、DDD基类、安全组件
│   ├── common-ddd/                   # DDD基类（AggregateRoot, Entity, ValueObject）
│   ├── common-redis/                 # Redis封装
│   └── common-web/                   # Web通用组件
├── gh-transport-gateway/             # API网关
│   ├── gateway-web/                  # Web端网关 (端口8080)
│   ├── gateway-mobile/               # Mobile端网关
│   └── gateway-admin/                # Admin端网关
├── gh-transport-auth/                # 认证授权限界上下文
├── gh-transport-customer/            # 客户限界上下文
│   ├── customer-domain/              # 领域层
│   ├── customer-application/         # 应用层
│   ├── customer-infrastructure/      # 基础设施层（MyBatis）
│   └── customer-interfaces/          # 接口层
├── gh-transport-order/               # 订单限界上下文（核心域）
│   ├── order-domain/                 # 领域层
│   ├── order-application/            # 应用层
│   ├── order-infrastructure/         # 基础设施层
│   └── order-interfaces/             # 接口层
├── gh-transport-dispatch/            # 调度限界上下文
│   ├── dispatch-domain/
│   ├── dispatch-application/
│   ├── dispatch-infrastructure/
│   └── dispatch-interfaces/
├── gh-transport-transport/           # 运输限界上下文
│   ├── transport-domain/
│   ├── transport-application/
│   ├── transport-infrastructure/     # MongoDB存储
│   └── transport-interfaces/
├── gh-transport-inventory/           # 库存限界上下文
│   ├── inventory-domain/
│   ├── inventory-application/
│   ├── inventory-infrastructure/
│   └── inventory-interfaces/
└── gh-transport-system/              # 系统限界上下文
```

## 模块说明

### 订单模块 (gh-transport-order)
- 创建订单、取消订单、确认订单、发货、完成订单
- 订单状态流转（待确认 → 已确认 → 已发货 → 已完成 → 已取消）
- 订单金额计算
- 订单项管理

### 客户模块 (gh-transport-customer)
- 客户CRUD
- 客户类型管理（企业/个人）
- 客户状态管理（启用/禁用）
- 客户搜索

### 调度模块 (gh-transport-dispatch)
- 调度单创建、分配
- 调度状态管理（待分配 → 已分配 → 运输中 → 已完成 → 已取消）
- 司机/车辆调度

### 运输模块 (gh-transport-transport)
- 运单管理（MongoDB存储）
- 位置更新
- 运输状态管理（提货中 → 运输中 → 配送中 → 已送达）
- 轨迹追踪

### 库存模块 (gh-transport-inventory)
- 库存CRUD
- 库存预留、扣减、释放
- SKU管理
- 仓库库存查询

### 认证模块 (gh-transport-auth)
- JWT Token生成与验证
- 用户认证
- 接口权限控制

## 已实现功能

### 核心组件
- **DDD基类**: AggregateRoot, Entity, ValueObject, DomainEvent
- **仓储模式**: Repository接口 + MyBatis/MongoDB实现
- **事务管理**: @Transactional统一配置
- **安全认证**: JWT + Spring Security
- **限流熔断**: Sentinel网关层 + 服务层限流
- **统一响应**: Result<T>统一返回格式
- **异常处理**: BusinessException统一异常处理

### 安全特性
- JWT Token生成、解析、验证
- Token刷新机制
- Spring Security配置
- 认证入口点（401处理）
- 接口权限控制

### 限流规则
| API类型 | QPS限制 |
|---------|---------|
| 认证API | 100 QPS |
| 订单/客户/调度/运输/库存API | 50 QPS |
| 单IP限流 | 10 QPS |

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

## 设计文档

- [架构设计](docs/plans/2025-01-29-gh-transport-ddd-project-design.md)

## License

MIT
