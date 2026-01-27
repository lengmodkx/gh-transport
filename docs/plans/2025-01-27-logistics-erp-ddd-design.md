# 物流ERP系统 - DDD领域驱动设计文档

> **版本：** v2.0.0-DDD
> **日期：** 2025-01-27
> **架构风格：** 领域驱动设计 (DDD) + CQRS

---

## 1. 战略设计

### 1.1 限界上下文划分

```
┌─────────────────────────────────────────────────────────────────┐
│                        物流ERP系统                               │
├──────────────┬──────────────┬──────────────┬──────────────────┤
│   订单上下文  │  调度上下文   │  运输上下文   │    客户上下文     │
│  (Order BC)  │ (Dispatch BC)│(Transport BC)│  (Customer BC)   │
├──────────────┴──────────────┴──────────────┴──────────────────┤
│   通用子域                                                       │
│   - 用户权限 (Auth)                                             │
│   - 系统管理 (System)                                           │
└─────────────────────────────────────────────────────────────────┘
```

| 限界上下文 | 英文 | 类型 | 核心领域概念 |
|-----------|------|------|-------------|
| 订单上下文 | Order Context | 核心域 | 运单、货物、订单生命周期 |
| 调度上下文 | Dispatch Context | 核心域 | 调度单、派车、路径规划 |
| 运输上下文 | Transport Context | 核心域 | 车辆、司机、运力管理 |
| 客户上下文 | Customer Context | 支撑域 | 客户、联系人、合同 |
| 库存上下文 | Inventory Context | 通用域 | 入库、出库、库存记录 |
| 用户上下文 | Auth Context | 通用域 | 用户、租户、权限 |

### 1.2 上下文映射

```
                    ┌─────────────────┐
                    │   Auth Context  │
                    └────────┬────────┘
                             │ 共享内核
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│Customer Context│◄───│ Order Context │───►│Dispatch Context│
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        │ 防腐层             │ 领域事件           │ 防腐层
        │                    │                    │
        └────────────────────┴────────────────────┘
                             │
                             ▼
                    ┌───────────────┐
                    │Transport Context│
                    └───────────────┘
```

| 关系 | 上游 | 下游 | 模式 | 说明 |
|-----|------|------|------|------|
| 客户-订单 | Customer | Order | 防腐层 | 订单通过ACL获取客户信息 |
| 订单-调度 | Order | Dispatch | 领域事件 | 订单创建触发调度 |
| 调度-运输 | Transport | Dispatch | 防腐层 | 调度查询可用运力 |
| 订单-库存 | Order | Inventory | 领域事件 | 订单出库触发库存变更 |

---

## 2. 战术设计

### 2.1 四层架构规范

```
gh-transport-order/                    # 限界上下文
├── order-application/                 # 应用层
│   ├── src/main/java/com/ghtransport/order/application/
│   │   ├── service/                   # ApplicationService
│   │   ├── command/                   # 命令对象
│   │   ├── query/                     # 查询对象 (CQRS)
│   │   ├── event/                     # 事件监听
│   │   ├── task/                      # 定时任务
│   │   └── dto/                       # 数据传输对象
│   └── pom.xml
├── order-domain/                      # 领域层 ⭐核心
│   ├── src/main/java/com/ghtransport/order/domain/
│   │   ├── aggregate/                 # 聚合根
│   │   ├── entity/                    # 实体
│   │   ├── valueobject/               # 值对象
│   │   ├── service/                   # DomainService
│   │   ├── event/                     # 领域事件
│   │   ├── repository/                # Repository接口
│   │   ├── specification/             # 规格
│   │   └── exception/                 # 领域异常
│   └── pom.xml
├── order-infrastructure/              # 基础设施层
│   ├── src/main/java/com/ghtransport/order/infrastructure/
│   │   ├── repository/                # Repository实现
│   │   ├── mapper/                    # MyBatisMapper
│   │   ├── external/                  # 防腐层(ACL)
│   │   ├── config/                    # 配置
│   │   └── messaging/                 # 消息发布
│   └── pom.xml
└── order-interfaces/                  # 接口层
    ├── src/main/java/com/ghtransport/order/interfaces/
    │   ├── controller/                # REST控制器
    │   ├── facade/                    # 外观(供其他BC调用)
    │   ├── dto/                       # 请求/响应DTO
    │   ├── assembler/                 # DTO-领域对象转换
    │   └── handler/                   # 异常处理
    └── pom.xml
```

### 2.2 模块依赖关系

```
interfaces ──► application ──► domain ◄── infrastructure
                                  ▲
                                  │
                             common-domain (共享)
```

**依赖规则：**
1. 上层依赖下层，下层不依赖上层
2. 领域层不依赖任何其他层
3. 基础设施通过依赖倒置接入

---

## 3. 核心领域模型

### 3.1 订单上下文 (Order Context)

#### 聚合根：Order (运单聚合)

```java
/**
 * 运单聚合根
 *
 * 聚合边界：
 * - Order (根)
 * - OrderItem (值对象列表)
 * - Cargo (值对象)
 * - Route (值对象)
 * - Money (值对象)
 */
public class Order extends AggregateRoot<OrderId> {

    // 标识
    private OrderId id;
    private OrderNo orderNo;

    // 关联
    private CustomerId customerId;

    // 值对象
    private List<OrderItem> items;
    private Route route;
    private Money totalAmount;
    private OrderStatus status;
    private TimeWindow timeWindow;

    // 创建工厂方法
    public static Order create(CreateOrderCmd cmd) {
        Order order = new Order();
        order.id = OrderId.generate();
        order.orderNo = OrderNo.generate();
        order.customerId = cmd.getCustomerId();
        order.route = new Route(cmd.getPickupAddress(), cmd.getDeliveryAddress());
        order.items = cmd.getItems().stream()
            .map(item -> OrderItem.create(item))
            .collect(Collectors.toList());
        order.totalAmount = calculateTotal(order.items);
        order.status = OrderStatus.PENDING;
        order.timeWindow = new TimeWindow(cmd.getPickupTime(), cmd.getDeliveryTime());

        // 注册领域事件
        order.registerEvent(new OrderCreatedEvent(order.id, order.orderNo));

        return order;
    }

    // 业务行为：取消订单
    public void cancel() {
        if (!status.isPending()) {
            throw new DomainException("只有待调度订单可以取消");
        }
        this.status = OrderStatus.CANCELLED;
        this.registerEvent(new OrderCancelledEvent(this.id));
    }

    // 业务行为：标记已调度
    public void markDispatched(DispatchId dispatchId) {
        if (!status.isPending()) {
            throw new DomainException("订单状态异常，无法调度");
        }
        this.status = OrderStatus.DISPATCHED;
        this.registerEvent(new OrderDispatchedEvent(this.id, dispatchId));
    }

    // 业务行为：开始运输
    public void startTransport() {
        if (!status.isDispatched()) {
            throw new DomainException("订单未调度，无法开始运输");
        }
        this.status = OrderStatus.IN_TRANSIT;
        this.registerEvent(new OrderTransportStartedEvent(this.id));
    }

    // 业务行为：送达确认
    public void markDelivered(DeliveryProof proof) {
        if (!status.isInTransit()) {
            throw new DomainException("订单不在运输中，无法确认送达");
        }
        this.status = OrderStatus.DELIVERED;
        this.registerEvent(new OrderDeliveredEvent(this.id, proof));
    }

    // 业务行为：完成订单
    public void complete() {
        if (!status.isDelivered()) {
            throw new DomainException("订单未送达，无法完成");
        }
        this.status = OrderStatus.COMPLETED;
        this.registerEvent(new OrderCompletedEvent(this.id));
    }

    // 业务规则验证
    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
}
```

#### 实体：OrderItem (货物明细)

```java
/**
 * 货物明细 - 值对象
 */
@ValueObject
public class OrderItem {
    private String itemName;
    private Quantity quantity;
    private Weight weight;
    private Volume volume;
    private Money unitPrice;
    private Money subtotal;

    public static OrderItem create(CreateOrderItemCmd cmd) {
        OrderItem item = new OrderItem();
        item.itemName = cmd.getItemName();
        item.quantity = new Quantity(cmd.getQuantity());
        item.weight = new Weight(cmd.getWeight());
        item.volume = new Volume(cmd.getVolume());
        item.unitPrice = new Money(cmd.getUnitPrice());
        item.subtotal = item.unitPrice.multiply(item.quantity.getValue());
        return item;
    }
}
```

#### 值对象：Address (地址)

```java
/**
 * 地址 - 值对象
 *
 * 不可变，包含完整地址验证逻辑
 */
@ValueObject
public class Address {
    private final String province;
    private final String city;
    private final String district;
    private final String street;
    private final String detail;
    private final String contactName;
    private final PhoneNumber contactPhone;

    public Address(String province, String city, String district,
                   String street, String detail,
                   String contactName, String contactPhone) {
        // 校验
        Validate.notBlank(province, "省份不能为空");
        Validate.notBlank(city, "城市不能为空");
        Validate.notBlank(detail, "详细地址不能为空");

        this.province = province;
        this.city = city;
        this.district = district;
        this.street = street;
        this.detail = detail;
        this.contactName = contactName;
        this.contactPhone = new PhoneNumber(contactPhone);
    }

    public String getFullAddress() {
        return String.format("%s%s%s%s%s",
            province, city, district, street, detail);
    }
}
```

#### 值对象：Money (金额)

```java
/**
 * 金额 - 值对象
 */
@ValueObject
public class Money {
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount) {
        this(amount, Currency.CNY);
    }

    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        checkCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    private void checkCurrency(Money other) {
        if (this.currency != other.currency) {
            throw new DomainException("货币类型不一致");
        }
    }
}
```

#### 领域事件

```java
// 订单创建事件
public class OrderCreatedEvent extends DomainEvent {
    private OrderId orderId;
    private OrderNo orderNo;
    private CustomerId customerId;
    private LocalDateTime createdAt;
}

// 订单取消事件
public class OrderCancelledEvent extends DomainEvent {
    private OrderId orderId;
    private String reason;
    private LocalDateTime cancelledAt;
}

// 订单已调度事件
public class OrderDispatchedEvent extends DomainEvent {
    private OrderId orderId;
    private DispatchId dispatchId;
    private LocalDateTime dispatchedAt;
}

// 订单运输开始事件
public class OrderTransportStartedEvent extends DomainEvent {
    private OrderId orderId;
    private LocalDateTime startedAt;
}

// 订单送达事件
public class OrderDeliveredEvent extends DomainEvent {
    private OrderId orderId;
    private DeliveryProof deliveryProof;
    private LocalDateTime deliveredAt;
}

// 订单完成事件
public class OrderCompletedEvent extends DomainEvent {
    private OrderId orderId;
    private LocalDateTime completedAt;
}
```

#### 枚举：OrderStatus (订单状态)

```java
public enum OrderStatus {
    PENDING("待调度") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == DISPATCHED || newStatus == CANCELLED;
        }
    },
    DISPATCHED("已调度") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == IN_TRANSIT || newStatus == CANCELLED;
        }
    },
    IN_TRANSIT("运输中") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == DELIVERED || newStatus == EXCEPTION;
        }
    },
    DELIVERED("已送达") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == COMPLETED || newStatus == EXCEPTION;
        }
    },
    COMPLETED("已完成") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false;
        }
    },
    CANCELLED("已取消") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false;
        }
    },
    EXCEPTION("异常") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == IN_TRANSIT || newStatus == COMPLETED;
        }
    };

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public abstract boolean canTransitionTo(OrderStatus newStatus);

    public void validateTransition(OrderStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new DomainException(
                String.format("订单状态不能从 %s 变更为 %s", this.description, newStatus.description)
            );
        }
    }
}
```

#### Repository接口

```java
public interface OrderRepository {
    Order findById(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
    List<Order> findByStatus(OrderStatus status);
    void save(Order order);
    void remove(Order order);
}
```

### 3.2 调度上下文 (Dispatch Context)

#### 聚合根：Dispatch (调度聚合)

```java
/**
 * 调度聚合根
 *
 * 负责：
 * 1. 运力的分配
 * 2. 调度状态管理
 * 3. 与订单上下文的协调
 */
public class Dispatch extends AggregateRoot<DispatchId> {

    private DispatchId id;
    private DispatchNo dispatchNo;

    // 关联 - 存储ID而非对象，保持边界清晰
    private OrderId orderId;
    private VehicleId vehicleId;
    private DriverId driverId;

    // 调度详情
    private DispatchStatus status;
    private TimeWindow plannedTime;
    private DispatchRoute route;

    // 执行记录
    private LocalDateTime dispatchedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // 创建工厂
    public static Dispatch create(OrderId orderId, VehicleId vehicleId,
                                   DriverId driverId, TimeWindow plannedTime) {
        Dispatch dispatch = new Dispatch();
        dispatch.id = DispatchId.generate();
        dispatch.dispatchNo = DispatchNo.generate();
        dispatch.orderId = orderId;
        dispatch.vehicleId = vehicleId;
        dispatch.driverId = driverId;
        dispatch.plannedTime = plannedTime;
        dispatch.status = DispatchStatus.PENDING;
        dispatch.dispatchedAt = LocalDateTime.now();

        dispatch.registerEvent(new DispatchCreatedEvent(
            dispatch.id, orderId, vehicleId, driverId
        ));

        return dispatch;
    }

    // 开始执行
    public void start() {
        if (!status.isPending()) {
            throw new DomainException("调度不是待执行状态");
        }
        this.status = DispatchStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();

        registerEvent(new DispatchStartedEvent(this.id, this.orderId));
    }

    // 完成调度
    public void complete() {
        if (!status.isInProgress()) {
            throw new DomainException("调度未开始，无法完成");
        }
        this.status = DispatchStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();

        registerEvent(new DispatchCompletedEvent(this.id, this.orderId));
    }

    // 取消调度
    public void cancel(String reason) {
        if (status.isCompleted()) {
            throw new DomainException("调度已完成，无法取消");
        }
        this.status = DispatchStatus.CANCELLED;

        registerEvent(new DispatchCancelledEvent(this.id, this.orderId, reason));
    }
}
```

#### 领域服务：DispatchService

```java
/**
 * 调度领域服务
 *
 * 处理跨聚合的业务逻辑
 */
@Service
public class DispatchDomainService {

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private TransportServiceClient transportClient;

    @Autowired
    private OrderServiceClient orderClient;

    /**
     * 智能调度
     *
     * 协调运输上下文获取可用运力
     * 创建调度并更新订单状态
     */
    public Dispatch autoDispatch(OrderId orderId, DispatchConstraint constraint) {
        // 1. 通过防腐层获取订单信息
        OrderInfo order = orderClient.getOrder(orderId);

        // 2. 通过防腐层获取可用运力
        List<Vehicle> availableVehicles = transportClient
            .findAvailableVehicles(order.getRoute(), order.getCargo());

        if (availableVehicles.isEmpty()) {
            throw new DomainException("暂无可用运力");
        }

        // 3. 选择最优车辆（简单策略：选择载重最接近的）
        Vehicle selectedVehicle = selectOptimalVehicle(availableVehicles, order.getCargo());

        // 4. 创建调度
        Dispatch dispatch = Dispatch.create(
            orderId,
            selectedVehicle.getId(),
            selectedVehicle.getDriverId(),
            constraint.getTimeWindow()
        );

        dispatchRepository.save(dispatch);

        return dispatch;
    }

    private Vehicle selectOptimalVehicle(List<Vehicle> vehicles, Cargo cargo) {
        return vehicles.stream()
            .min(Comparator.comparing(v ->
                Math.abs(v.getMaxWeight().subtract(cargo.getWeight()).doubleValue())))
            .orElseThrow();
    }
}
```

### 3.3 运输上下文 (Transport Context)

#### 聚合根：Vehicle (车辆聚合)

```java
public class Vehicle extends AggregateRoot<VehicleId> {

    private VehicleId id;
    private PlateNumber plateNumber;
    private VehicleType type;
    private Capacity capacity;  // 载重、容积
    private VehicleStatus status;
    private DriverId currentDriverId;

    // 运力占用记录
    private List<OccupancyRecord> occupancyRecords;

    // 业务行为：检查可用性
    public boolean isAvailableFor(TimeWindow timeWindow, Cargo cargo) {
        if (!status.isAvailable()) {
            return false;
        }

        // 检查载重
        if (capacity.getMaxWeight().isLessThan(cargo.getWeight())) {
            return false;
        }

        // 检查时间冲突
        return occupancyRecords.stream()
            .noneMatch(record -> record.conflictsWith(timeWindow));
    }

    // 业务行为：占用运力
    public void occupy(TimeWindow timeWindow, OrderId orderId) {
        occupancyRecords.add(new OccupancyRecord(timeWindow, orderId));
    }

    // 业务行为：释放运力
    public void release(OrderId orderId) {
        occupancyRecords.removeIf(record -> record.getOrderId().equals(orderId));
    }
}
```

### 3.4 客户上下文 (Customer Context)

#### 聚合根：Customer

```java
public class Customer extends AggregateRoot<CustomerId> {

    private CustomerId id;
    private CustomerCode customerCode;
    private CustomerName customerName;
    private CustomerType type;

    // 值对象集合
    private List<Contact> contacts;
    private Address mainAddress;
    private CreditLimit creditLimit;

    // 业务行为：添加联系人
    public void addContact(Contact contact) {
        if (contact.isPrimary()) {
            // 只能有一个主联系人
            contacts.forEach(c -> c.setPrimary(false));
        }
        contacts.add(contact);
    }

    // 业务行为：检查信用额度
    public boolean canPlaceOrder(Money orderAmount) {
        return creditLimit.isWithinLimit(orderAmount);
    }
}
```

---

## 4. CQRS设计

### 4.1 命令端与查询端分离

```
┌─────────────────────────────────────────────────────────────┐
│                          客户端                              │
└──────────────────────┬──────────────────────────────────────┘
                       │
          ┌────────────┴────────────┐
          ▼                         ▼
┌──────────────────┐      ┌──────────────────┐
│   Command API    │      │    Query API     │
│  (写模型-事务性)  │      │   (读模型-优化)   │
└────────┬─────────┘      └────────┬─────────┘
         │                         │
         ▼                         ▼
┌──────────────────┐      ┌──────────────────┐
│  Application     │      │   Query Service  │
│  Service         │      │   (直接查DB)      │
└────────┬─────────┘      └────────┬─────────┘
         │                         │
         ▼                         ▼
┌──────────────────┐      ┌──────────────────┐
│   Domain Layer   │      │   Read Model     │
│   (聚合根)        │      │   (物化视图)      │
└────────┬─────────┘      └────────┬─────────┘
         │                         │
         ▼                         ▼
┌──────────────────┐      ┌──────────────────┐
│   Repository     │      │   Query Mapper   │
│   (写库)         │      │   (读库/ES)       │
└──────────────────┘      └──────────────────┘
```

### 4.2 命令对象

```java
// 创建订单命令
@Data
public class CreateOrderCmd {
    private CustomerId customerId;
    private Address pickupAddress;
    private Address deliveryAddress;
    private List<CreateOrderItemCmd> items;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
}

// 取消订单命令
@Data
public class CancelOrderCmd {
    private OrderId orderId;
    private String reason;
}
```

### 4.3 查询对象

```java
// 订单列表查询
@Data
public class OrderListQuery {
    private OrderStatus status;
    private CustomerId customerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int pageNum = 1;
    private int pageSize = 10;
}

// 订单列表VO（读模型）
@Data
public class OrderListVO {
    private String orderNo;
    private String customerName;
    private String status;
    private String pickupAddress;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
```

### 4.4 读模型数据同步

```java
/**
 * 订单事件监听 - 同步读模型
 */
@Component
public class OrderReadModelUpdater {

    @Autowired
    private OrderReadMapper readMapper;

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        // 插入读模型
        readMapper.insert(new OrderListVO(...));
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        // 更新读模型
        readMapper.updateStatus(event.getOrderId(), event.getNewStatus());
    }
}
```

---

## 5. 防腐层设计

### 5.1 防腐层接口定义

```java
/**
 * 订单服务防腐层接口
 * 位于Dispatch BC，依赖Order BC
 */
public interface OrderServiceClient {

    /**
     * 获取订单信息
     */
    OrderInfo getOrder(OrderId orderId);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(OrderId orderId, OrderStatus status);

    /**
     * 检查订单是否存在
     */
    boolean exists(OrderId orderId);
}

/**
 * 运输服务防腐层接口
 * 位于Dispatch BC，依赖Transport BC
 */
public interface TransportServiceClient {

    /**
     * 查找可用车辆
     */
    List<VehicleInfo> findAvailableVehicles(Route route, Cargo cargo);

    /**
     * 锁定车辆
     */
    void lockVehicle(VehicleId vehicleId, TimeWindow timeWindow);

    /**
     * 释放车辆
     */
    void unlockVehicle(VehicleId vehicleId);
}
```

### 5.2 防腐层实现

```java
@Component
public class OrderServiceClientImpl implements OrderServiceClient {

    @Autowired
    private OrderFeignClient feignClient;

    @Autowired
    private OrderInfoConverter converter;

    @Override
    public OrderInfo getOrder(OrderId orderId) {
        try {
            OrderDTO dto = feignClient.getOrder(orderId.getValue());
            return converter.toOrderInfo(dto);
        } catch (FeignException.NotFound e) {
            throw new DomainException("订单不存在: " + orderId);
        } catch (FeignException e) {
            throw new InfrastructureException("订单服务调用失败", e);
        }
    }

    @Override
    public void updateOrderStatus(OrderId orderId, OrderStatus status) {
        feignClient.updateStatus(orderId.getValue(), status.name());
    }
}
```

---

## 6. 事件驱动架构

### 6.1 事件总线设计

```java
/**
 * 领域事件发布器
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}

/**
 * Spring事件实现
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}

/**
 * 聚合根基类 - 支持事件注册
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {

    @JsonIgnore
    private transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

### 6.2 跨上下文事件流

```
┌─────────────────────────────────────────────────────────────────┐
│                      事件流转示意图                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Order Context]              [Dispatch Context]               │
│                                                                 │
│  OrderCreatedEvent ────────►  创建自动调度                      │
│                                                                 │
│  OrderCancelledEvent ─────►   取消关联调度                      │
│                                                                 │
│  ◄─────────── DispatchCreatedEvent                              │
│  更新订单为已调度                                               │
│                                                                 │
│  ◄─────────── DispatchStartedEvent                              │
│  更新订单为运输中                                               │
│                                                                 │
│  ◄─────────── DispatchCompletedEvent                            │
│  更新订单为已送达                                               │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [Order Context]              [Inventory Context]              │
│                                                                 │
│  OrderCreatedEvent ────────►  预留库存                          │
│  OrderCancelledEvent ─────►   释放库存                          │
│  OrderDeliveredEvent ─────►   确认出库                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. 基础设施实现

### 7.1 Repository实现

```java
@Component
public class OrderRepositoryImpl implements OrderRepository {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper itemMapper;

    @Autowired
    private EventStore eventStore;

    @Override
    public Order findById(OrderId id) {
        // 查询聚合根
        OrderPO po = orderMapper.selectById(id.getValue());
        if (po == null) {
            return null;
        }

        // 查询子对象
        List<OrderItemPO> items = itemMapper.selectByOrderId(id.getValue());

        // 转换为领域对象
        return OrderConverter.toDomain(po, items);
    }

    @Override
    public void save(Order order) {
        // 保存聚合根
        OrderPO po = OrderConverter.toPO(order);
        if (order.isNew()) {
            orderMapper.insert(po);
        } else {
            orderMapper.updateById(po);
        }

        // 保存子对象
        saveItems(order);

        // 发布领域事件
        publishEvents(order);
    }

    private void publishEvents(Order order) {
        List<DomainEvent> events = order.getDomainEvents();
        events.forEach(eventPublisher::publish);
        order.clearDomainEvents();
    }
}
```

### 7.2 MyBatis配置

```java
@Configuration
@MapperScan("com.ghtransport.order.infrastructure.mapper")
public class MyBatisConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler()));
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

---

## 8. 接口层设计

### 8.1 REST控制器

```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderAppService;
    private final OrderQueryService orderQueryService;

    /**
     * 创建订单 - Command
     */
    @PostMapping
    public Result<OrderId> create(@RequestBody @Valid CreateOrderRequest request) {
        CreateOrderCmd cmd = OrderAssembler.toCreateCmd(request);
        OrderId orderId = orderAppService.createOrder(cmd);
        return Result.success(orderId);
    }

    /**
     * 取消订单 - Command
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable String orderId,
                                @RequestBody CancelOrderRequest request) {
        CancelOrderCmd cmd = new CancelOrderCmd(new OrderId(orderId), request.getReason());
        orderAppService.cancelOrder(cmd);
        return Result.success();
    }

    /**
     * 查询订单列表 - Query (CQRS)
     */
    @GetMapping
    public Result<PageResult<OrderListVO>> list(OrderListQuery query) {
        PageResult<OrderListVO> result = orderQueryService.queryOrderList(query);
        return Result.success(result);
    }

    /**
     * 查询订单详情 - Query (CQRS)
     */
    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> detail(@PathVariable String orderId) {
        OrderDetailVO detail = orderQueryService.queryOrderDetail(new OrderId(orderId));
        return Result.success(detail);
    }
}
```

### 8.2 DTO转换器

```java
@Component
public class OrderAssembler {

    public static CreateOrderCmd toCreateCmd(CreateOrderRequest request) {
        CreateOrderCmd cmd = new CreateOrderCmd();
        cmd.setCustomerId(new CustomerId(request.getCustomerId()));
        cmd.setPickupAddress(toAddress(request.getPickupAddress()));
        cmd.setDeliveryAddress(toAddress(request.getDeliveryAddress()));
        cmd.setItems(request.getItems().stream()
            .map(OrderAssembler::toItemCmd)
            .collect(Collectors.toList()));
        cmd.setPickupTime(request.getPickupTime());
        cmd.setDeliveryTime(request.getDeliveryTime());
        return cmd;
    }

    public static OrderDetailVO toDetailVO(Order order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrderNo(order.getOrderNo().getValue());
        vo.setStatus(order.getStatus().getDescription());
        vo.setItems(order.getItems().stream()
            .map(OrderAssembler::toItemVO)
            .collect(Collectors.toList()));
        return vo;
    }
}
```

---

## 9. 应用层设计

### 9.1 ApplicationService

```java
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final DomainEventPublisher eventPublisher;

    /**
     * 创建订单
     *
     * 应用服务职责：
     * 1. 接收命令
     * 2. 协调领域对象
     * 3. 事务控制
     * 4. 持久化
     */
    @Transactional
    public OrderId createOrder(CreateOrderCmd cmd) {
        // 1. 业务检查（通过领域服务）
        orderDomainService.checkCustomerExists(cmd.getCustomerId());

        // 2. 创建聚合根
        Order order = Order.create(cmd);

        // 3. 持久化
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(CancelOrderCmd cmd) {
        // 1. 加载聚合根
        Order order = orderRepository.findById(cmd.getOrderId());
        if (order == null) {
            throw new DomainException("订单不存在");
        }

        // 2. 执行业务操作
        order.cancel();

        // 3. 持久化
        orderRepository.save(order);
    }
}
```

---

## 10. 技术栈更新

### 10.1 项目结构

```
gh-transport-parent/
├── gh-transport-common/
│   └── src/main/java/com/ghtransport/common/
│       ├── domain/                    # 共享领域基类
│       │   ├── AggregateRoot.java
│       │   ├── Entity.java
│       │   ├── ValueObject.java
│       │   └── DomainEvent.java
│       └── exception/
├── gh-transport-gateway/
├── gh-transport-auth/
├── gh-transport-customer/
│   ├── customer-domain/
│   ├── customer-application/
│   ├── customer-infrastructure/
│   └── customer-interfaces/
├── gh-transport-order/
│   ├── order-domain/                  # 领域层 (纯Java)
│   ├── order-application/             # 应用层
│   ├── order-infrastructure/          # 基础设施层
│   └── order-interfaces/              # 接口层
├── gh-transport-dispatch/
│   ├── dispatch-domain/
│   ├── dispatch-application/
│   ├── dispatch-infrastructure/
│   └── dispatch-interfaces/
├── gh-transport-transport/            # 运输上下文 (原vehicle)
│   ├── transport-domain/
│   ├── transport-application/
│   ├── transport-infrastructure/
│   └── transport-interfaces/
├── gh-transport-inventory/
└── gh-transport-system/
```

### 10.2 依赖管理

```xml
<!-- 父POM关键依赖 -->
<dependencyManagement>
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.2.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>8.0.1.Final</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## 11. 数据库设计 (DDD视角)

### 11.1 订单上下文表

```sql
-- 运单表 (聚合根)
CREATE TABLE t_order (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,

    -- 收发地址 (值对象序列化或展开)
    pickup_province VARCHAR(50),
    pickup_city VARCHAR(50),
    pickup_district VARCHAR(50),
    pickup_detail VARCHAR(200),
    pickup_contact VARCHAR(50),
    pickup_phone VARCHAR(20),

    delivery_province VARCHAR(50),
    delivery_city VARCHAR(50),
    delivery_district VARCHAR(50),
    delivery_detail VARCHAR(200),
    delivery_contact VARCHAR(50),
    delivery_phone VARCHAR(20),

    -- 时间窗口
    pickup_time DATETIME,
    delivery_time DATETIME,

    -- 金额 (值对象)
    total_amount DECIMAL(18,2),
    currency VARCHAR(3) DEFAULT 'CNY',

    -- 状态
    order_status VARCHAR(20) DEFAULT 'PENDING',

    -- 版本号 (乐观锁)
    version INT DEFAULT 0,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,

    INDEX idx_customer_id (customer_id),
    INDEX idx_status (order_status),
    INDEX idx_created_at (created_at)
) COMMENT '运单表';

-- 货物明细 (从属实体)
CREATE TABLE t_order_item (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    weight DECIMAL(10,2),
    volume DECIMAL(10,2),
    unit_price DECIMAL(18,2),
    subtotal DECIMAL(18,2),
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES t_order(id),
    INDEX idx_order_id (order_id)
) COMMENT '货物明细表';

-- 领域事件存储 (事件溯源支持)
CREATE TABLE t_domain_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    event_data JSON NOT NULL,
    version INT NOT NULL,
    occurred_at DATETIME NOT NULL,
    published TINYINT DEFAULT 0,
    INDEX idx_aggregate (aggregate_type, aggregate_id),
    INDEX idx_published (published)
) COMMENT '领域事件表';
```

---

## 12. 开发阶段规划

### Phase 1: 基础架构 (Week 1)
- [ ] 创建Maven多模块项目结构
- [ ] 实现共享领域基类
- [ ] 配置数据库连接池
- [ ] 实现领域事件发布机制
- [ ] 配置CQRS读写分离

### Phase 2: 订单上下文 (Week 2)
- [ ] 实现Order聚合根
- [ ] 实现值对象 (Address, Money, Quantity等)
- [ ] 实现Repository
- [ ] 实现ApplicationService
- [ ] 实现REST接口

### Phase 3: 调度上下文 (Week 3)
- [ ] 实现Dispatch聚合根
- [ ] 实现防腐层 (ACL)
- [ ] 实现领域事件监听
- [ ] 实现跨上下文协调

### Phase 4: 其他上下文 (Week 4)
- [ ] 运输上下文 (车辆/司机)
- [ ] 客户上下文
- [ ] 库存上下文
- [ ] 系统管理

### Phase 5: 集成与优化 (Week 5)
- [ ] 事件总线集成
- [ ] 读模型优化
- [ ] 性能测试
- [ ] 文档完善

---

## 13. 领域服务设计

### 13.1 订单领域服务

```java
/**
 * 订单领域服务
 * 处理跨聚合的复杂业务逻辑
 */
public interface OrderDomainService {

    /**
     * 检查客户是否有效
     */
    void checkCustomerExists(CustomerId customerId);

    /**
     * 验证订单可以创建
     */
    Result<Void> validateCreateOrder(CreateOrderCmd cmd);
}
```

```java
@Service
public class OrderDomainServiceImpl implements OrderDomainService {

    private final CustomerRepository customerRepository;

    public OrderDomainServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void checkCustomerExists(CustomerId customerId) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new DomainException("客户不存在");
        }
        if (!customer.isActive()) {
            throw new DomainException("客户已禁用");
        }
    }

    @Override
    public Result<Void> validateCreateOrder(CreateOrderCmd cmd) {
        // 校验信用额度
        Money orderAmount = calculateOrderAmount(cmd);
        if (!customer.canPlaceOrder(orderAmount)) {
            return Result.error("EXCEED_CREDIT_LIMIT", "超出信用额度");
        }
        return Result.success();
    }

    private Money calculateOrderAmount(CreateOrderCmd cmd) {
        // 计算订单金额逻辑
    }
}
```

---

## 14. 规格模式应用

### 14.1 规格接口

```java
/**
 * 规格模式接口
 * 用于封装业务规则，支持组合
 */
public interface Specification<T> {

    /**
     * 规格是否满足
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * 与操作
     */
    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }

    /**
     * 或操作
     */
    default Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }

    /**
     * 非操作
     */
    default Specification<T> not() {
        return new NotSpecification<>(this);
    }
}
```

### 14.2 复合规格实现

```java
/**
 * And规格
 */
public class AndSpecification<T> implements Specification<T> {
    private final Specification<T> left;
    private final Specification<T> right;

    public AndSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
    }
}

/**
 * Or规格
 */
public class OrSpecification<T> implements Specification<T> {
    private final Specification<T> left;
    private final Specification<T> right;

    public OrSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
    }
}
```

### 14.3 业务规格应用

```java
// 订单可调度规格
public class OrderCanBeDispatchedSpec implements Specification<Order> {

    @Override
    public boolean isSatisfiedBy(Order order) {
        return order.getStatus().isPending()
            && order.getPickupTime() != null
            && order.getDeliveryTime() != null;
    }
}

// 使用示例
public void dispatchOrder(Order order) {
    Specification<Order> spec = new OrderCanBeDispatchedSpec();
    if (!spec.isSatisfiedBy(order)) {
        throw new DomainException("订单不满足调度条件");
    }
    // 执行调度...
}
```

---

## 15. Saga流程管理器

### 15.1 订单-调度Saga

```java
/**
 * 订单调度Saga
 * 处理跨聚合的复杂事务
 */
@Component
public class OrderDispatchSaga {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private TransportServiceClient transportClient;

    /**
     * 执行调度Saga
     *
     * 事务步骤：
     * 1. 创建调度
     * 2. 锁定车辆
     * 3. 更新订单状态
     *
     * 如果任何步骤失败，执行补偿操作
     */
    public DispatchResult execute(CreateDispatchCmd cmd) {
        try {
            // Step 1: 创建调度
            Dispatch dispatch = Dispatch.create(...);
            dispatchRepository.save(dispatch);

            // Step 2: 锁定车辆
            transportClient.lockVehicle(cmd.getVehicleId(), dispatch.getPlannedTime());

            // Step 3: 更新订单状态
            order.markDispatched(dispatch.getId());
            orderRepository.save(order);

            return DispatchResult.success(dispatch.getId());

        } catch (Exception e) {
            // 补偿操作
            compensate(dispatch);
            throw new DomainException("调度失败: " + e.getMessage());
        }
    }

    private void compensate(Dispatch dispatch) {
        // 回滚操作
        transportClient.unlockVehicle(dispatch.getVehicleId());
        if (dispatch != null) {
            dispatch.cancel("调度失败自动取消");
            dispatchRepository.save(dispatch);
        }
    }
}
```

---

## 16. 多租户在DDD中的处理

### 16.1 租户上下文注入

```java
/**
 * 租户感知仓储
 * 确保所有查询都包含租户过滤
 */
public abstract class TenantAwareRepository<T> {

    @Autowired
    private TenantContext tenantContext;

    protected Long getCurrentTenantId() {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new DomainException("租户上下文不存在");
        }
        return tenantId;
    }

    protected void addTenantFilter(LambdaQueryWrapper<T> wrapper) {
        wrapper.eq(getTableTenantColumn(), getCurrentTenantId());
    }

    protected abstract String getTableTenantColumn();
}

/**
 * 租户上下文持有者
 */
@Component
public class TenantContext {

    private static final ThreadLocal<Long> CONTEXT = new ThreadLocal<>();

    public static void set(Long tenantId) {
        CONTEXT.set(tenantId);
    }

    public static Long getCurrentTenantId() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
```

### 16.2 领域层使用

```java
// Order聚合中访问租户ID
public class Order extends AggregateRoot<String> {

    private Long tenantId;  // 多租户标识

    public static Order create(CreateOrderCmd cmd) {
        Order order = new Order();
        order.id = OrderId.generate().getValue();
        order.tenantId = TenantContext.getCurrentTenantId();  // 获取当前租户
        order.status = OrderStatus.PENDING;
        // ...
        return order;
    }
}
```

---

## 17. ID生成策略

### 17.1 ID类型定义

```java
/**
 * 订单ID - 雪花算法
 */
public class OrderId {
    private final String value;

    private OrderId(String value) {
        this.value = value;
    }

    public static OrderId generate() {
        return new OrderId(SnowflakeIdGenerator.generate());
    }

    public static OrderId fromString(String value) {
        return new OrderId(value);
    }

    public String getValue() {
        return value;
    }
}

/**
 * 雪花算法ID生成器
 */
@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1704067200000L; // 2024-01-01
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    public static synchronized String generate() {
        long timestamp = System.currentTimeMillis() - EPOCH;
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 4095;
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        return String.valueOf((timestamp << 22) | (sequence));
    }
}
```

---

## 18. 值对象持久化策略

### 18.1 值对象映射策略

```java
/**
 * 值对象持久化策略
 *
 * 1. 展开映射：Address -> pickup_province, pickup_city...
 * 2. JSON序列化：ComplexVO -> json字符串
 * 3. 嵌入表：简单的值对象嵌入主表
 */
@Entity
@Table(name = "t_order")
public class OrderPO {

    // 值对象展开存储
    @Column(name = "pickup_province")
    private String pickupProvince;

    @Column(name = "pickup_city")
    private String pickupCity;

    // JSON存储（复杂值对象）
    @Column(name = "route_info", columnDefinition = "JSON")
    private String routeInfo;

    // Money值对象
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency")
    private String currency;
}
```

### 18.2 转换器

```java
@Component
public class OrderConverter {

    public static Order toDomain(OrderPO po, List<OrderItemPO> items) {
        Order order = new Order();
        order.setId(po.getId());
        order.setOrderNo(new OrderNo(po.getOrderNo()));

        // 值对象展开转换
        order.setPickupAddress(new Address(
            po.getPickupProvince(),
            po.getPickupCity(),
            po.getPickupDistrict(),
            po.getPickupDetail(),
            po.getPickupContact(),
            po.getPickupPhone()
        ));

        // Money值对象转换
        order.setTotalAmount(new Money(po.getTotalAmount(), Currency.of(po.getCurrency())));

        return order;
    }

    public static OrderPO toPO(Order order) {
        OrderPO po = new OrderPO();
        po.setId(order.getId());
        po.setOrderNo(order.getOrderNo().getValue());

        // 值对象展开存储
        Address pickup = order.getPickupAddress();
        po.setPickupProvince(pickup.getProvince());
        po.setPickupCity(pickup.getCity());
        // ...

        // Money值对象
        po.setTotalAmount(order.getTotalAmount().getAmount());
        po.setCurrency(order.getTotalAmount().getCurrency().name());

        return po;
    }
}
```

---

## 19. 事件溯源与重放

### 19.1 事件存储

```java
/**
 * 领域事件存储
 */
@Component
public class EventStore {

    @Autowired
    private DomainEventMapper eventMapper;

    /**
     * 保存事件
     */
    public void save(DomainEvent event, String aggregateId, String aggregateType) {
        DomainEventPO po = new DomainEventPO();
        po.setEventId(event.getEventId());
        po.setEventType(event.getClass().getSimpleName());
        po.setAggregateId(aggregateId);
        po.setAggregateType(aggregateType);
        po.setEventData(JsonUtils.toJson(event));
        po.setOccurredAt(event.getOccurredAt());
        po.setPublished(false);
        eventMapper.insert(po);
    }

    /**
     * 获取聚合所有事件
     */
    public List<DomainEvent> getEvents(String aggregateId, String aggregateType) {
        List<DomainEventPO> events = eventMapper.selectByAggregate(aggregateId, aggregateType);
        return events.stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    /**
     * 重放事件重建聚合
     */
    public Order rebuildOrder(String orderId) {
        List<DomainEvent> events = getEvents(orderId, "Order");
        Order order = new Order();  // 空聚合根

        for (DomainEvent event : events) {
            order = applyEvent(order, event);
        }

        return order;
    }

    private Order applyEvent(Order order, DomainEvent event) {
        if (event instanceof OrderCreatedEvent) {
            // 应用事件
            order.apply((OrderCreatedEvent) event);
        }
        // ...
        return order;
    }
}
```

---

## 20. 异常分类

### 20.1 领域异常体系

```java
/**
 * 领域异常基类
 */
public abstract class DomainException extends RuntimeException {
    private final String errorCode;
    private final ErrorType errorType;

    public DomainException(String errorCode, String message, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    public enum ErrorType {
        BUSINESS,      // 业务异常（可预期）
        INFRASTRUCTURE, // 基础设施异常
        VALIDATION     // 校验异常
    }
}

/**
 * 业务异常
 */
public class BusinessException extends DomainException {
    public BusinessException(String errorCode, String message) {
        super(errorCode, message, ErrorType.BUSINESS);
    }
}

/**
 * 业务规则违规
 */
public class BusinessRuleViolationException extends DomainException {
    public BusinessRuleViolationException(String message) {
        super("BUSINESS_RULE_VIOLATION", message, ErrorType.BUSINESS);
    }
}
```

---

**文档状态：** 已补充遗漏内容
**下一步：** 更新实施计划，补充测试和配置任务
