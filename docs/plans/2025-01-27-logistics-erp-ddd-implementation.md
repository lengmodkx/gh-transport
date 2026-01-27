# 物流ERP系统DDD实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标：** 构建基于DDD领域驱动设计的物流ERP系统MVP

**架构：** 四层架构（接口层/应用层/领域层/基础设施层）+ CQRS + 事件驱动

**技术栈：** Java 17, Spring Boot 3.2, MyBatis-Plus, Spring Event, MapStruct, validation-api

---

## 目录结构

```
gh-transport-parent/
├── gh-transport-common/              # 共享领域基类
├── gh-transport-gateway/
├── gh-transport-auth/
├── gh-transport-customer/
│   ├── customer-domain/              # 纯Java，无Spring依赖
│   ├── customer-application/
│   ├── customer-infrastructure/
│   └── customer-interfaces/
├── gh-transport-order/               # 核心域
│   ├── order-domain/
│   ├── order-application/
│   ├── order-infrastructure/
│   └── order-interfaces/
├── gh-transport-dispatch/
│   ├── dispatch-domain/
│   ├── dispatch-application/
│   ├── dispatch-infrastructure/
│   └── dispatch-interfaces/
├── gh-transport-transport/           # 运输上下文（原vehicle）
│   ├── transport-domain/
│   ├── transport-application/
│   ├── transport-infrastructure/
│   └── transport-interfaces/
├── gh-transport-inventory/
└── gh-transport-system/
```

---

## Phase 1: 基础架构

### Task 1: 创建Maven多模块项目（DDD结构）

**Files:**
- Create: `pom.xml`
- Create: `gh-transport-common/pom.xml`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/domain/AggregateRoot.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/domain/Entity.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/domain/ValueObject.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/domain/DomainEvent.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/domain/DomainEventPublisher.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/exception/DomainException.java`

**Step 1: 创建父POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ghtransport</groupId>
    <artifactId>gh-transport-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>GH Transport Parent</name>
    <description>物流ERP系统 - DDD架构</description>

    <modules>
        <module>gh-transport-common</module>
        <module>gh-transport-gateway</module>
        <module>gh-transport-auth</module>
        <module>gh-transport-customer</module>
        <module>gh-transport-order</module>
        <module>gh-transport-dispatch</module>
        <module>gh-transport-transport</module>
        <module>gh-transport-inventory</module>
        <module>gh-transport-system</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.2.0</spring-boot.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <jwt.version>0.12.3</jwt.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>

            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>

            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jwt.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.11.0</version>
                    <configuration>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>org.mapstruct</groupId>
                                <artifactId>mapstruct-processor</artifactId>
                                <version>${mapstruct.version}</version>
                            </path>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                                <version>1.18.30</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

**Step 2: 创建common模块POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ghtransport</groupId>
        <artifactId>gh-transport-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>gh-transport-common</artifactId>
    <name>GH Transport Common</name>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Step 3: 创建领域基类 - DomainEvent**

```java
package com.ghtransport.common.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 */
@Getter
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
    }
}
```

**Step 4: 创建领域基类 - Entity**

```java
package com.ghtransport.common.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 实体基类
 */
@Getter
@Setter
public abstract class Entity<ID> {

    protected ID id;

    protected abstract ID generateId();

    public boolean isNew() {
        return id == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return id != null && id.equals(entity.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
```

**Step 5: 创建领域基类 - AggregateRoot**

```java
package com.ghtransport.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
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

**Step 6: 创建领域基类 - ValueObject**

```java
package com.ghtransport.common.domain;

/**
 * 值对象标记接口
 */
public interface ValueObject {
    // 值对象通过属性值比较相等性
}
```

**Step 7: 创建领域事件发布器接口**

```java
package com.ghtransport.common.domain;

/**
 * 领域事件发布器
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
```

**Step 8: 创建领域异常**

```java
package com.ghtransport.common.exception;

import lombok.Getter;

/**
 * 领域异常
 */
@Getter
public class DomainException extends RuntimeException {

    private final String errorCode;

    public DomainException(String message) {
        super(message);
        this.errorCode = "DOMAIN_ERROR";
    }

    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

**Step 9: 创建工具类 - Validate**

```java
package com.ghtransport.common.util;

import com.ghtransport.common.exception.DomainException;

/**
 * 校验工具
 */
public class Validate {

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new DomainException(message);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new DomainException(message);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new DomainException(message);
        }
    }
}
```

**Step 10: Commit**

```bash
git add pom.xml gh-transport-common/
git commit -m "chore: 初始化DDD项目结构，添加领域基类"
```

---

### Task 2: 创建订单上下文模块结构

**Files:**
- Create: `gh-transport-order/order-domain/pom.xml`
- Create: `gh-transport-order/order-application/pom.xml`
- Create: `gh-transport-order/order-infrastructure/pom.xml`
- Create: `gh-transport-order/order-interfaces/pom.xml`
- Create: `gh-transport-order/pom.xml`

**Step 1: 创建order父模块POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ghtransport</groupId>
        <artifactId>gh-transport-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>gh-transport-order</artifactId>
    <packaging>pom</packaging>
    <name>GH Transport Order Context</name>

    <modules>
        <module>order-domain</module>
        <module>order-application</module>
        <module>order-infrastructure</module>
        <module>order-interfaces</module>
    </modules>
</project>
```

**Step 2: 创建order-domain模块POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ghtransport</groupId>
        <artifactId>gh-transport-order</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>order-domain</artifactId>
    <name>Order Domain</name>

    <dependencies>
        <dependency>
            <groupId>com.ghtransport</groupId>
            <artifactId>gh-transport-common</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

**Step 3: 创建order-application模块POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ghtransport</groupId>
        <artifactId>gh-transport-order</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>order-application</artifactId>
    <name>Order Application</name>

    <dependencies>
        <dependency>
            <groupId>com.ghtransport</groupId>
            <artifactId>order-domain</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Step 4: 创建其他模块（类似结构）**

**Step 5: Commit**

```bash
git add gh-transport-order/
git commit -m "chore: 创建订单上下文四层模块结构"
```

---

### Task 3: 实现订单领域层

**Files:**
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/aggregate/Order.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/entity/OrderItem.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/vo/Address.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/vo/Money.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/vo/OrderNo.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/vo/OrderStatus.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/event/OrderCreatedEvent.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/event/OrderCancelledEvent.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/repository/OrderRepository.java`

**Step 1: 创建值对象 - Money**

```java
package com.ghtransport.order.domain.vo;

import com.ghtransport.common.domain.ValueObject;
import com.ghtransport.common.exception.DomainException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@EqualsAndHashCode
public class Money implements ValueObject {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount) {
        this(amount, Currency.CNY);
    }

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new DomainException("金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("金额不能为负数");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency != null ? currency : Currency.CNY;
    }

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    private void checkCurrency(Money other) {
        if (this.currency != other.currency) {
            throw new DomainException("货币类型不一致");
        }
    }
}
```

**Step 2: 创建值对象 - Address**

```java
package com.ghtransport.order.domain.vo;

import com.ghtransport.common.domain.ValueObject;
import com.ghtransport.common.util.Validate;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Address implements ValueObject {

    private final String province;
    private final String city;
    private final String district;
    private final String detail;
    private final String contactName;
    private final String contactPhone;

    public Address(String province, String city, String district,
                   String detail, String contactName, String contactPhone) {
        Validate.notBlank(province, "省份不能为空");
        Validate.notBlank(city, "城市不能为空");
        Validate.notBlank(detail, "详细地址不能为空");

        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
    }

    public String getFullAddress() {
        return String.format("%s%s%s%s", province, city, district, detail);
    }
}
```

**Step 3: 创建枚举 - OrderStatus**

```java
package com.ghtransport.order.domain.vo;

import com.ghtransport.common.exception.DomainException;
import lombok.Getter;

@Getter
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

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isDispatched() {
        return this == DISPATCHED;
    }

    public boolean isInTransit() {
        return this == IN_TRANSIT;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }
}
```

**Step 4: 创建领域事件**

```java
package com.ghtransport.order.domain.event;

import com.ghtransport.common.domain.DomainEvent;
import lombok.Getter;

@Getter
public class OrderCreatedEvent extends DomainEvent {

    private final String orderId;
    private final String orderNo;
    private final String customerId;

    public OrderCreatedEvent(String orderId, String orderNo, String customerId) {
        super();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.customerId = customerId;
    }
}
```

```java
package com.ghtransport.order.domain.event;

import com.ghtransport.common.domain.DomainEvent;
import lombok.Getter;

@Getter
public class OrderCancelledEvent extends DomainEvent {

    private final String orderId;
    private final String reason;

    public OrderCancelledEvent(String orderId, String reason) {
        super();
        this.orderId = orderId;
        this.reason = reason;
    }
}
```

**Step 5: 创建聚合根 - Order**

```java
package com.ghtransport.order.domain.aggregate;

import com.ghtransport.common.domain.AggregateRoot;
import com.ghtransport.common.util.Validate;
import com.ghtransport.order.domain.entity.OrderItem;
import com.ghtransport.order.domain.event.OrderCancelledEvent;
import com.ghtransport.order.domain.event.OrderCreatedEvent;
import com.ghtransport.order.domain.vo.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Order extends AggregateRoot<String> {

    private OrderNo orderNo;
    private String customerId;
    private List<OrderItem> items;
    private Address pickupAddress;
    private Address deliveryAddress;
    private Money totalAmount;
    private OrderStatus status;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 工厂方法
    public static Order create(String customerId, List<OrderItem> items,
                                Address pickupAddress, Address deliveryAddress,
                                LocalDateTime pickupTime, LocalDateTime deliveryTime) {
        Validate.notBlank(customerId, "客户ID不能为空");
        Validate.isTrue(items != null && !items.isEmpty(), "货物明细不能为空");

        Order order = new Order();
        order.id = UUID.randomUUID().toString();
        order.orderNo = OrderNo.generate();
        order.customerId = customerId;
        order.items = new ArrayList<>(items);
        order.pickupAddress = pickupAddress;
        order.deliveryAddress = deliveryAddress;
        order.totalAmount = calculateTotal(items);
        order.status = OrderStatus.PENDING;
        order.pickupTime = pickupTime;
        order.deliveryTime = deliveryTime;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();

        order.registerEvent(new OrderCreatedEvent(order.id, order.orderNo.getValue(), customerId));

        return order;
    }

    // 业务行为：取消订单
    public void cancel(String reason) {
        if (!status.isPending()) {
            throw new DomainException("只能取消待调度状态的订单");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        this.registerEvent(new OrderCancelledEvent(this.id, reason));
    }

    // 业务行为：标记已调度
    public void markDispatched() {
        status.validateTransition(OrderStatus.DISPATCHED);
        this.status = OrderStatus.DISPATCHED;
        this.updatedAt = LocalDateTime.now();
    }

    // 业务行为：开始运输
    public void startTransport() {
        status.validateTransition(OrderStatus.IN_TRANSIT);
        this.status = OrderStatus.IN_TRANSIT;
        this.updatedAt = LocalDateTime.now();
    }

    // 业务行为：标记送达
    public void markDelivered() {
        status.validateTransition(OrderStatus.DELIVERED);
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }

    // 业务行为：完成订单
    public void complete() {
        status.validateTransition(OrderStatus.COMPLETED);
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
}
```

**Step 6: 创建Repository接口**

```java
package com.ghtransport.order.domain.repository;

import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.vo.OrderStatus;

import java.util.List;

public interface OrderRepository {

    Order findById(String id);

    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(OrderStatus status);

    void save(Order order);
}
```

**Step 7: Commit**

```bash
git add gh-transport-order/order-domain/
git commit -m "feat(order-domain): 实现订单聚合根、值对象和领域事件"
```

---

### Task 4: 实现订单应用层

**Files:**
- Create: `order-application/src/main/java/com/ghtransport/order/application/service/OrderApplicationService.java`
- Create: `order-application/src/main/java/com/ghtransport/order/application/command/CreateOrderCmd.java`
- Create: `order-application/src/main/java/com/ghtransport/order/application/command/CancelOrderCmd.java`
- Create: `order-application/src/main/java/com/ghtransport/order/application/dto/OrderDTO.java`

**Step 1: 创建命令对象**

```java
package com.ghtransport.order.application.command;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOrderCmd {
    private String customerId;
    private String pickupProvince;
    private String pickupCity;
    private String pickupDistrict;
    private String pickupDetail;
    private String pickupContact;
    private String pickupPhone;
    private String deliveryProvince;
    private String deliveryCity;
    private String deliveryDistrict;
    private String deliveryDetail;
    private String deliveryContact;
    private String deliveryPhone;
    private List<OrderItemCmd> items;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
}
```

```java
package com.ghtransport.order.application.command;

import lombok.Data;

@Data
public class CancelOrderCmd {
    private String orderId;
    private String reason;
}
```

**Step 2: 创建ApplicationService**

```java
package com.ghtransport.order.application.service;

import com.ghtransport.common.domain.DomainEventPublisher;
import com.ghtransport.common.exception.DomainException;
import com.ghtransport.order.application.command.CancelOrderCmd;
import com.ghtransport.order.application.command.CreateOrderCmd;
import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.entity.OrderItem;
import com.ghtransport.order.domain.repository.OrderRepository;
import com.ghtransport.order.domain.vo.Address;
import com.ghtransport.order.domain.vo.Money;
import com.ghtransport.order.domain.vo.Quantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public String createOrder(CreateOrderCmd cmd) {
        // 构建值对象
        Address pickupAddress = new Address(
            cmd.getPickupProvince(), cmd.getPickupCity(),
            cmd.getPickupDistrict(), cmd.getPickupDetail(),
            cmd.getPickupContact(), cmd.getPickupPhone()
        );

        Address deliveryAddress = new Address(
            cmd.getDeliveryProvince(), cmd.getDeliveryCity(),
            cmd.getDeliveryDistrict(), cmd.getDeliveryDetail(),
            cmd.getDeliveryContact(), cmd.getDeliveryPhone()
        );

        // 构建货物明细
        var items = cmd.getItems().stream()
            .map(itemCmd -> OrderItem.create(
                itemCmd.getItemName(),
                new Quantity(itemCmd.getQuantity()),
                itemCmd.getWeight(),
                itemCmd.getVolume(),
                new Money(itemCmd.getUnitPrice())
            ))
            .collect(Collectors.toList());

        // 创建聚合根
        Order order = Order.create(
            cmd.getCustomerId(),
            items,
            pickupAddress,
            deliveryAddress,
            cmd.getPickupTime(),
            cmd.getDeliveryTime()
        );

        // 持久化
        orderRepository.save(order);

        // 发布领域事件
        order.getDomainEvents().forEach(eventPublisher::publish);
        order.clearDomainEvents();

        return order.getId();
    }

    @Transactional
    public void cancelOrder(CancelOrderCmd cmd) {
        Order order = orderRepository.findById(cmd.getOrderId());
        if (order == null) {
            throw new DomainException("订单不存在");
        }

        order.cancel(cmd.getReason());
        orderRepository.save(order);

        // 发布领域事件
        order.getDomainEvents().forEach(eventPublisher::publish);
        order.clearDomainEvents();
    }
}
```

**Step 3: Commit**

```bash
git add gh-transport-order/order-application/
git commit -m "feat(order-application): 实现订单应用服务"
```

---

### Task 5: 实现订单基础设施层

**Files:**
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/repository/OrderRepositoryImpl.java`
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/mapper/OrderMapper.java`
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/messaging/SpringDomainEventPublisher.java`

**Step 1: 创建Repository实现**

```java
package com.ghtransport.order.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.repository.OrderRepository;
import com.ghtransport.order.domain.vo.OrderStatus;
import com.ghtransport.order.infrastructure.mapper.OrderMapper;
import com.ghtransport.order.infrastructure.po.OrderPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;

    @Override
    public Order findById(String id) {
        OrderPO po = orderMapper.selectById(id);
        return po != null ? OrderConverter.toDomain(po) : null;
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderPO::getCustomerId, customerId);
        return orderMapper.selectList(wrapper).stream()
            .map(OrderConverter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderPO::getStatus, status.name());
        return orderMapper.selectList(wrapper).stream()
            .map(OrderConverter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void save(Order order) {
        OrderPO po = OrderConverter.toPO(order);
        if (order.isNew()) {
            orderMapper.insert(po);
        } else {
            orderMapper.updateById(po);
        }
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-order/order-infrastructure/
git commit -m "feat(order-infrastructure): 实现订单仓储"
```

---

### Task 6: 实现订单接口层

**Files:**
- Create: `order-interfaces/src/main/java/com/ghtransport/order/interfaces/controller/OrderController.java`
- Create: `order-interfaces/src/main/java/com/ghtransport/order/interfaces/dto/CreateOrderRequest.java`
- Create: `order-interfaces/src/main/java/com/ghtransport/order/interfaces/assembler/OrderAssembler.java`
- Create: `order-interfaces/src/main/java/com/ghtransport/order/interfaces/exception/OrderExceptionHandler.java`

**Step 1: 创建REST控制器**

```java
package com.ghtransport.order.interfaces.controller;

import com.ghtransport.common.result.Result;
import com.ghtransport.order.application.command.CancelOrderCmd;
import com.ghtransport.order.application.command.CreateOrderCmd;
import com.ghtransport.order.application.service.OrderApplicationService;
import com.ghtransport.order.interfaces.assembler.OrderAssembler;
import com.ghtransport.order.interfaces.dto.CancelOrderRequest;
import com.ghtransport.order.interfaces.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderAppService;

    @PostMapping
    public Result<String> create(@RequestBody CreateOrderRequest request) {
        CreateOrderCmd cmd = OrderAssembler.toCreateCmd(request);
        String orderId = orderAppService.createOrder(cmd);
        return Result.success(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable String orderId,
                               @RequestBody CancelOrderRequest request) {
        CancelOrderCmd cmd = new CancelOrderCmd();
        cmd.setOrderId(orderId);
        cmd.setReason(request.getReason());
        orderAppService.cancelOrder(cmd);
        return Result.success();
    }
}
```

**Step 2: 创建异常处理器**

```java
package com.ghtransport.order.interfaces.exception;

import com.ghtransport.common.exception.DomainException;
import com.ghtransport.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class OrderExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public Result<Void> handleDomainException(DomainException e) {
        log.error("Order domain exception: {}", e.getMessage());
        return Result.error(e.getErrorCode(), e.getMessage());
    }
}
```

**Step 3: Commit**

```bash
git add gh-transport-order/order-interfaces/
git commit -m "feat(order-interfaces): 实现订单REST接口和异常处理"
```

---

### Task 7: 实现DomainEventPublisher和事件监听

**Files:**
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/messaging/SpringDomainEventPublisher.java`
- Create: `order-application/src/main/java/com/ghtransport/order/application/event/OrderEventListener.java`

**Step 1: 创建Spring事件发布器**

```java
package com.ghtransport.order.infrastructure.messaging;

import com.ghtransport.common.domain.DomainEvent;
import com.ghtransport.common.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {}", event.getClass().getSimpleName());
        eventPublisher.publishEvent(event);
    }
}
```

**Step 2: 创建领域事件监听器**

```java
package com.ghtransport.order.application.event;

import com.ghtransport.order.domain.event.OrderCreatedEvent;
import com.ghtransport.order.domain.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Order created: orderId={}, orderNo={}, customerId={}",
            event.getOrderId(), event.getOrderNo(), event.getCustomerId());
        // 这里可以触发其他业务逻辑，如发送通知等
    }

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Order cancelled: orderId={}, reason={}",
            event.getOrderId(), event.getReason());
    }
}
```

**Step 3: Commit**

```bash
git add order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/messaging/
git add order-application/src/main/java/com/ghtransport/order/application/event/
git commit -m "feat(order): 实现领域事件发布和监听"
```

---

### Task 8: 实现订单查询服务 (CQRS读模型)

**Files:**
- Create: `order-application/src/main/java/com/ghtransport/order/application/query/OrderQueryService.java`
- Create: `order-application/src/main/java/com/ghtransport/order/application/query/OrderListQuery.java`
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/query/OrderReadMapper.java`

**Step 1: 创建查询服务**

```java
package com.ghtransport.order.application.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.order.infrastructure.query.OrderReadMapper;
import com.ghtransport.order.infrastructure.query.OrderListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderReadMapper readMapper;

    public Page<OrderListVO> queryOrderList(OrderListQuery query) {
        LambdaQueryWrapper<OrderListVO> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(OrderListVO::getStatus, query.getStatus());
        }
        if (query.getCustomerId() != null) {
            wrapper.eq(OrderListVO::getCustomerId, query.getCustomerId());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(OrderListVO::getCreatedAt, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(OrderListVO::getCreatedAt, query.getEndDate().atTime(23, 59, 59));
        }

        wrapper.orderByDesc(OrderListVO::getCreatedAt);

        Page<OrderListVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return readMapper.selectPage(page, wrapper);
    }
}
```

**Step 2: 创建读模型VO**

```java
package com.ghtransport.order.infrastructure.query;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("v_order_list")
public class OrderListVO {

    private String id;
    private String orderNo;
    private String customerId;
    private String customerName;
    private String status;

    @TableField(exist = false)
    private String statusDesc;

    private String pickupAddress;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
```

**Step 3: Commit**

```bash
git add order-application/src/main/java/com/ghtransport/order/application/query/
git add order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/query/
git commit -m "feat(order): 实现订单查询服务和读模型"
```

---

### Task 9: 实现防腐层 (ACL)

**Files:**
- Create: `dispatch-infrastructure/src/main/java/com/ghtransport/dispatch/infrastructure/external/OrderServiceClient.java`
- Create: `dispatch-infrastructure/src/main/java/com/ghtransport/dispatch/infrastructure/external/OrderServiceClientImpl.java`
- Create: `dispatch-infrastructure/src/main/java/com/ghtransport/dispatch/infrastructure/external/TransportServiceClient.java`
- Create: `dispatch-infrastructure/src/main/java/com/ghtransport/dispatch/infrastructure/external/TransportServiceClientImpl.java`

**Step 1: 创建防腐层接口**

```java
package com.ghtransport.dispatch.infrastructure.external;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单服务防腐层接口
 */
public interface OrderServiceClient {

    OrderInfo getOrder(String orderId);

    void updateOrderStatus(String orderId, String status);

    boolean exists(String orderId);
}

@Data
class OrderInfo {
    private String orderId;
    private String customerId;
    private String pickupAddress;
    private String deliveryAddress;
    private BigDecimal weight;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
}
```

**Step 2: 创建防腐层实现**

```java
package com.ghtransport.dispatch.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderServiceClientImpl implements OrderServiceClient {

    @Override
    public OrderInfo getOrder(String orderId) {
        log.debug("Fetching order info from external service: {}", orderId);
        // 实际实现中，这里会调用FeignClient或其他远程服务
        // 由于是防腐层，外部服务的异常会被转换为领域异常
        return null; // 简化实现
    }

    @Override
    public void updateOrderStatus(String orderId, String status) {
        log.debug("Updating order status: orderId={}, status={}", orderId, status);
    }

    @Override
    public boolean exists(String orderId) {
        return getOrder(orderId) != null;
    }
}
```

**Step 3: Commit**

```bash
git add dispatch-infrastructure/src/main/java/com/ghtransport/dispatch/infrastructure/external/
git commit -m "feat(dispatch): 实现防腐层(ACL)"
```

---

### Task 10: 实现数据库配置

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/config/MybatisPlusConfig.java`
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/config/DatabaseConfig.java`

**Step 1: 创建MyBatis-Plus配置**

```java
package com.ghtransport.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/config/
git commit -m "feat(common): 实现MyBatis-Plus配置"
```

---

### Task 11: 实现Auth上下文

**Files:**
- Create: `gh-transport-auth/auth-domain/src/main/java/com/ghtransport/auth/domain/aggregate/User.java`
- Create: `gh-transport-auth/auth-domain/src/main/java/com/ghtransport/auth/domain/aggregate/Role.java`
- Create: `gh-transport-auth/auth-domain/src/main/java/com/ghtransport/auth/domain/repository/UserRepository.java`
- Create: `gh-transport-auth/auth-application/src/main/java/com/ghtransport/auth/application/service/AuthApplicationService.java`

**Step 1: 创建用户聚合根**

```java
package com.ghtransport.auth.domain.aggregate;

import com.ghtransport.common.domain.AggregateRoot;
import com.ghtransport.common.util.Validate;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class User extends AggregateRoot<String> {

    private String username;
    private String passwordHash;
    private String email;
    private String phone;
    private Set<String> roleIds;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static User create(String username, String passwordHash, String email) {
        Validate.notBlank(username, "用户名不能为空");
        Validate.notBlank(passwordHash, "密码不能为空");

        User user = new User();
        user.id = UUID.randomUUID().toString();
        user.username = username;
        user.passwordHash = passwordHash;
        user.email = email;
        user.roleIds = new HashSet<>();
        user.enabled = true;
        user.createdAt = LocalDateTime.now();

        return user;
    }

    public void assignRole(String roleId) {
        this.roleIds.add(roleId);
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-auth/
git commit -m "feat(auth): 实现用户和角色聚合根"
```

---

### Task 12: 实现基础设施层配置

**Files:**
- Create: `order-infrastructure/pom.xml` (补充依赖)
- Create: `order-infrastructure/src/main/resources/application.yml`
- Create: `order-interfaces/src/main/resources/application.yml`

**Step 1: 创建基础设施层POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ghtransport</groupId>
        <artifactId>gh-transport-order</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>order-infrastructure</artifactId>
    <name>Order Infrastructure</name>

    <dependencies>
        <dependency>
            <groupId>com.ghtransport</groupId>
            <artifactId>order-application</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

**Step 2: Commit**

```bash
git add gh-transport-order/order-infrastructure/pom.xml
git commit -m "feat(order-infrastructure): 添加基础设施层配置"
```

---

## Phase 2: DDD高级模式

### Task 13: 实现领域服务与规格模式

**Files:**
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/service/OrderDomainService.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/service/OrderDomainServiceImpl.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/specification/OrderSpecification.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/specification/AndSpecification.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/specification/OrSpecification.java`

**Step 1: 创建规格接口**

```java
package com.ghtransport.order.domain.specification;

import com.ghtransport.order.domain.aggregate.Order;

public interface OrderSpecification {
    boolean isSatisfiedBy(Order order);
}
```

**Step 2: 创建具体规格 - 待调度订单规格**

```java
package com.ghtransport.order.domain.specification;

import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.vo.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class PendingOrderSpecification implements OrderSpecification {

    @Override
    public boolean isSatisfiedBy(Order order) {
        return order.getStatus() == OrderStatus.PENDING;
    }
}
```

**Step 3: 创建And组合规格**

```java
package com.ghtransport.order.domain.specification;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AndSpecification implements OrderSpecification {

    private final OrderSpecification left;
    private final OrderSpecification right;

    @Override
    public boolean isSatisfiedBy(Order order) {
        return left.isSatisfiedBy(order) && right.isSatisfiedBy(order);
    }
}
```

**Step 4: 创建Or组合规格**

```java
package com.ghtransport.order.domain.specification;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrSpecification implements OrderSpecification {

    private final OrderSpecification left;
    private final OrderSpecification right;

    @Override
    public boolean isSatisfiedBy(Order order) {
        return left.isSatisfiedBy(order) || right.isSatisfiedBy(order);
    }
}
```

**Step 5: 创建领域服务接口**

```java
package com.ghtransport.order.domain.service;

import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.specification.OrderSpecification;

import java.util.List;

public interface OrderDomainService {

    List<Order> findOrders(OrderSpecification specification);

    void validateOrderForDispatch(String orderId);

    void validateOrderForCancellation(String orderId);
}
```

**Step 6: Commit**

```bash
git add order-domain/src/main/java/com/ghtransport/order/domain/specification/
git add order-domain/src/main/java/com/ghtransport/order/domain/service/
git commit -m "feat(order-domain): 实现规格模式和领域服务"
```

---

### Task 14: 实现Saga流程管理器

**Files:**
- Create: `order-application/src/main/java/com/ghtransport/order/application/saga/OrderDispatchSaga.java`
- Create: `order-application/src/main/java/com/ghtransport/order/application/saga/OrderDispatchSagaState.java`
- Create: `order-application/src/main/java/com/ghtransport/order/application/saga/OrderDispatchSagaOrchestrator.java`

**Step 1: 创建Saga状态**

```java
package com.ghtransport.order.application.saga;

public enum OrderDispatchSagaState {
    STARTED,
    ORDER_VALIDATED,
    DISPATCH_CREATED,
    VEHICLE_ASSIGNED,
    DRIVER_NOTIFIED,
    COMPLETED,
    FAILED,
    COMPENSATING,
    ROLLED_BACK
}
```

**Step 2: 创建Saga数据**

```java
package com.ghtransport.order.application.saga;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class OrderDispatchSagaData {
    private String orderId;
    private String orderNo;
    private String customerId;
    private String dispatchId;
    private String vehicleId;
    private String driverId;
    private OrderDispatchSagaState currentState;
    private String failureReason;
}
```

**Step 3: 创建Saga编排器**

```java
package com.ghtransport.order.application.saga;

import com.ghtransport.common.domain.DomainEvent;
import com.ghtransport.order.domain.event.OrderDispatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDispatchSagaOrchestrator {

    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, OrderDispatchSagaData> sagaStore = new ConcurrentHashMap<>();

    public String startSaga(String orderId, String orderNo, String customerId) {
        String sagaId = "saga-" + orderId;

        OrderDispatchSagaData data = OrderDispatchSagaData.builder()
            .orderId(orderId)
            .orderNo(orderNo)
            .customerId(customerId)
            .currentState(OrderDispatchSagaState.STARTED)
            .build();

        sagaStore.put(sagaId, data);
        log.info("Started OrderDispatchSaga: {} for order: {}", sagaId, orderId);

        return sagaId;
    }

    public void processStep(String sagaId, OrderDispatchSagaState nextState) {
        OrderDispatchSagaData data = sagaStore.get(sagaId);
        if (data == null) {
            throw new IllegalArgumentException("Saga not found: " + sagaId);
        }

        data.setCurrentState(nextState);
        log.info("Saga {} progressed to state: {}", sagaId, nextState);
    }

    public void compensate(String sagaId, String reason) {
        OrderDispatchSagaData data = sagaStore.get(sagaId);
        if (data == null) {
            log.warn("Saga not found for compensation: {}", sagaId);
            return;
        }

        data.setCurrentState(OrderDispatchSagaState.COMPENSATING);
        data.setFailureReason(reason);

        // 回滚调度单
        if (data.getDispatchId() != null) {
            // 调用Dispatch领域服务取消调度单
            log.info("Compensating: canceling dispatch {}", data.getDispatchId());
        }

        data.setCurrentState(OrderDispatchSagaState.ROLLED_BACK);
        log.info("Saga {} compensated: {}", sagaId, reason);
    }

    public void complete(String sagaId) {
        OrderDispatchSagaData data = sagaStore.get(sagaId);
        if (data != null) {
            data.setCurrentState(OrderDispatchSagaState.COMPLETED);
            log.info("Saga {} completed", sagaId);
        }
    }
}
```

**Step 4: Commit**

```bash
git add order-application/src/main/java/com/ghtransport/order/application/saga/
git commit -m "feat(order-application): 实现订单调度Saga流程管理器"
```

---

### Task 15: 实现多租户支持

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/tenant/TenantContext.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/tenant/TenantAware.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/repository/TenantAwareOrderRepository.java`

**Step 1: 创建租户上下文**

```java
package com.ghtransport.common.tenant;

public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
```

**Step 2: 创建租户感知接口**

```java
package com.ghtransport.common.tenant;

public interface TenantAware {

    String getTenantId();

    void setTenantId(String tenantId);
}
```

**Step 3: 创建租户感知仓储**

```java
package com.ghtransport.order.domain.repository;

import com.ghtransport.order.domain.aggregate.Order;

public interface TenantAwareOrderRepository extends OrderRepository {

    Order findByIdAndTenant(String id, String tenantId);

    void save(Order order, String tenantId);
}
```

**Step 4: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/tenant/
git commit -m "feat(common): 实现多租户上下文"
```

---

### Task 16: 实现ID生成策略

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/id/SnowflakeIdGenerator.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/id/OrderNoGenerator.java`
- Create: `order-domain/src/main/java/com/ghtransport/order/domain/vo/OrderNo.java`

**Step 1: 创建雪花算法ID生成器**

```java
package com.ghtransport.common.id;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class SnowflakeIdGenerator {

    private static final long EPOCH = LocalDateTime.of(2025, 1, 1, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this.datacenterId = 1L;
        this.workerId = 1L;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            log.error("Clock moved backwards. Refusing to generate id for {}ms", lastTimestamp - timestamp);
            throw new RuntimeException("Clock moved backwards");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
```

**Step 2: 创建订单号生成器**

```java
package com.ghtransport.order.domain.vo;

import com.ghtransport.common.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OrderNoGenerator {

    private final SnowflakeIdGenerator idGenerator;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public OrderNo generate() {
        String datePrefix = LocalDate.now().format(FORMATTER);
        long sequence = idGenerator.nextId() % 100000;
        String orderNo = String.format("ORD%s%05d", datePrefix, sequence);
        return new OrderNo(orderNo);
    }
}
```

**Step 3: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/id/
git commit -m "feat(common): 实现雪花算法ID生成器"
```

---

### Task 17: 实现值对象持久化策略

**Files:**
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/repository/OrderConverter.java`
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/vo/JacksonUtils.java`

**Step 1: 创建值对象序列化工具**

```java
package com.ghtransport.order.infrastructure.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtransport.common.domain.ValueObject;

public class JacksonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serialize(ValueObject vo) {
        try {
            return objectMapper.writeValueAsString(vo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize value object", e);
        }
    }

    public static <T extends ValueObject> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize value object", e);
        }
    }
}
```

**Step 2: 创建PO转换器**

```java
package com.ghtransport.order.infrastructure.repository;

import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.entity.OrderItem;
import com.ghtransport.order.domain.vo.*;
import com.ghtransport.order.infrastructure.po.OrderPO;
import com.ghtransport.order.infrastructure.vo.JacksonUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderConverter {

    public Order toDomain(OrderPO po) {
        List<OrderItem> items = JacksonUtils.deserialize(po.getItemsJson(), OrderItem[].class);

        Order order = Order.create(
            po.getCustomerId(),
            items,
            JacksonUtils.deserialize(po.getPickupAddressJson(), Address.class),
            JacksonUtils.deserialize(po.getDeliveryAddressJson(), Address.class),
            po.getPickupTime(),
            po.getDeliveryTime()
        );

        // 使用反射设置ID和状态
        try {
            java.lang.reflect.Field idField = Order.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, po.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set order id", e);
        }

        return order;
    }

    public OrderPO toPO(Order order) {
        OrderPO po = new OrderPO();
        po.setId(order.getId());
        po.setOrderNo(order.getOrderNo().getValue());
        po.setCustomerId(order.getCustomerId());
        po.setItemsJson(JacksonUtils.serialize(order.getItems()));
        po.setPickupAddressJson(JacksonUtils.serialize(order.getPickupAddress()));
        po.setDeliveryAddressJson(JacksonUtils.serialize(order.getDeliveryAddress()));
        po.setTotalAmount(order.getTotalAmount().getAmount().toString());
        po.setStatus(order.getStatus().name());
        po.setPickupTime(order.getPickupTime());
        po.setDeliveryTime(order.getDeliveryTime());
        po.setCreatedAt(order.getCreatedAt());
        po.setUpdatedAt(order.getUpdatedAt());
        return po;
    }
}
```

**Step 3: Commit**

```bash
git add order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/repository/OrderConverter.java
git commit -m "feat(order-infrastructure): 实现值对象序列化与PO转换"
```

---

### Task 18: 实现事件溯源

**Files:**
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/eventstore/EventStore.java`
- Create: `order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/eventstore/OrderEventStore.java`

**Step 1: 创建事件存储接口**

```java
package com.ghtransport.order.infrastructure.eventstore;

import com.ghtransport.common.domain.DomainEvent;

import java.util.List;

public interface OrderEventStore {

    void append(String aggregateId, DomainEvent event);

    List<DomainEvent> loadEvents(String aggregateId);

    void append(String aggregateId, List<DomainEvent> events);
}
```

**Step 2: 创建事件存储实现**

```java
package com.ghtransport.order.infrastructure.eventstore;

import com.ghtransport.common.domain.DomainEvent;
import com.ghtransport.order.infrastructure.mapper.OrderEventMapper;
import com.ghtransport.order.infrastructure.po.OrderEventPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderEventStore implements OrderEventStore {

    private final OrderEventMapper eventMapper;

    @Override
    public void append(String aggregateId, DomainEvent event) {
        OrderEventPO po = new OrderEventPO();
        po.setAggregateId(aggregateId);
        po.setEventType(event.getClass().getSimpleName());
        po.setEventData(serializeEvent(event));
        po.setOccurredAt(event.getOccurredAt());

        eventMapper.insert(po);
    }

    @Override
    public List<DomainEvent> loadEvents(String aggregateId) {
        List<OrderEventPO> events = eventMapper.selectByAggregateId(aggregateId);
        return events.stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    @Override
    public void append(String aggregateId, List<DomainEvent> events) {
        events.forEach(event -> append(aggregateId, event));
    }

    private String serializeEvent(DomainEvent event) {
        // 使用JSON序列化
        return "";
    }

    private DomainEvent deserializeEvent(OrderEventPO po) {
        // 根据事件类型反序列化
        return null;
    }
}
```

**Step 3: Commit**

```bash
git add order-infrastructure/src/main/java/com/ghtransport/order/infrastructure/eventstore/
git commit -m "feat(order-infrastructure): 实现事件存储"
```

---

### Task 19: 实现异常层次结构

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/exception/BusinessException.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/exception/BusinessRuleViolationException.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/exception/ErrorCode.java`

**Step 1: 创建业务异常**

```java
package com.ghtransport.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends DomainException {

    private final String errorCode;
    private final String message;

    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
```

**Step 2: 创建业务规则违规异常**

```java
package com.ghtransport.common.exception;

import lombok.Getter;

@Getter
public class BusinessRuleViolationException extends DomainException {

    private final String ruleName;
    private final String details;

    public BusinessRuleViolationException(String ruleName, String details) {
        super("BUSINESS_RULE_VIOLATION",
            String.format("业务规则[%s]被违反: %s", ruleName, details));
        this.ruleName = ruleName;
        this.details = details;
    }
}
```

**Step 3: 创建错误码枚举**

```java
package com.ghtransport.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    ORDER_NOT_FOUND("ORDER_001", "订单不存在"),
    ORDER_CANNOT_CANCEL("ORDER_002", "订单无法取消"),
    ORDER_STATUS_INVALID("ORDER_003", "订单状态无效"),
    DISPATCH_NOT_FOUND("DISPATCH_001", "调度单不存在"),
    VEHICLE_NOT_FOUND("VEHICLE_001", "车辆不存在"),
    CUSTOMER_NOT_FOUND("CUSTOMER_001", "客户不存在");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

**Step 4: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/exception/
git commit -m "feat(common): 实现异常层次结构和错误码"
```

---

## Phase 3: 其他限界上下文

### Task 20: 实现调度上下文 (Dispatch BC)

**Files:**
- Create: `gh-transport-dispatch/dispatch-domain/src/main/java/com/ghtransport/dispatch/domain/aggregate/Dispatch.java`
- Create: `gh-transport-dispatch/dispatch-domain/src/main/java/com/ghtransport/dispatch/domain/vo/DispatchStatus.java`
- Create: `gh-transport-dispatch/dispatch-application/src/main/java/com/ghtransport/dispatch/application/service/DispatchApplicationService.java`

**Step 1: 创建调度聚合根**

```java
package com.ghtransport.dispatch.domain.aggregate;

import com.ghtransport.common.domain.AggregateRoot;
import com.ghtransport.common.util.Validate;
import com.ghtransport.dispatch.domain.vo.DispatchStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Dispatch extends AggregateRoot<String> {

    private String orderId;
    private String vehicleId;
    private String driverId;
    private DispatchStatus status;
    private LocalDateTime dispatchTime;
    private LocalDateTime estimatedArrival;
    private String notes;

    public static Dispatch create(String orderId, String vehicleId, String driverId) {
        Validate.notBlank(orderId, "订单ID不能为空");
        Validate.notBlank(vehicleId, "车辆ID不能为空");

        Dispatch dispatch = new Dispatch();
        dispatch.id = UUID.randomUUID().toString();
        dispatch.orderId = orderId;
        dispatch.vehicleId = vehicleId;
        dispatch.driverId = driverId;
        dispatch.status = DispatchStatus.CREATED;
        dispatch.dispatchTime = LocalDateTime.now();

        return dispatch;
    }

    public void assignDriver(String driverId) {
        Validate.notBlank(driverId, "司机ID不能为空");
        this.driverId = driverId;
        this.status = DispatchStatus.DRIVER_ASSIGNED;
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-dispatch/
git commit -m "feat(dispatch): 实现调度聚合根"
```

---

### Task 21: 实现运输上下文 (Transport BC)

**Files:**
- Create: `gh-transport-transport/transport-domain/src/main/java/com/ghtransport/transport/domain/aggregate/Vehicle.java`
- Create: `gh-transport-transport/transport-domain/src/main/java/com/ghtransport/transport/domain/aggregate/Driver.java`

**Step 1: 创建车辆聚合根**

```java
package com.ghtransport.transport.domain.aggregate;

import com.ghtransport.common.domain.AggregateRoot;
import com.ghtransport.transport.domain.vo.VehicleStatus;
import com.ghtransport.transport.domain.vo.VehicleType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Vehicle extends AggregateRoot<String> {

    private String plateNumber;
    private String brand;
    private String model;
    private VehicleType type;
    private BigDecimal capacity;
    private VehicleStatus status;
    private LocalDate registrationDate;
    private LocalDate nextMaintenanceDate;

    public static Vehicle create(String plateNumber, String brand, String model,
                                  VehicleType type, BigDecimal capacity) {
        Vehicle vehicle = new Vehicle();
        vehicle.id = UUID.randomUUID().toString();
        vehicle.plateNumber = plateNumber;
        vehicle.brand = brand;
        vehicle.model = model;
        vehicle.type = type;
        vehicle.capacity = capacity;
        vehicle.status = VehicleStatus.AVAILABLE;
        vehicle.registrationDate = LocalDate.now();

        return vehicle;
    }

    public void markInMaintenance() {
        this.status = VehicleStatus.MAINTENANCE;
    }

    public void markAvailable() {
        this.status = VehicleStatus.AVAILABLE;
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-transport/
git commit -m "feat(transport): 实现车辆聚合根"
```

---

### Task 22: 实现客户上下文 (Customer BC)

**Files:**
- Create: `gh-transport-customer/customer-domain/src/main/java/com/ghtransport/customer/domain/aggregate/Customer.java`
- Create: `gh-transport-customer/customer-domain/src/main/java/com/ghtransport/customer/domain/vo/CustomerType.java`

**Step 1: 创建客户聚合根**

```java
package com.ghtransport.customer.domain.aggregate;

import com.ghtransport.common.domain.AggregateRoot;
import com.ghtransport.common.util.Validate;
import com.ghtransport.customer.domain.vo.Address;
import com.ghtransport.customer.domain.vo.CustomerType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Customer extends AggregateRoot<String> {

    private String name;
    private String phone;
    private String email;
    private Address address;
    private CustomerType type;
    private boolean active;
    private LocalDateTime createdAt;

    public static Customer create(String name, String phone, CustomerType type) {
        Validate.notBlank(name, "客户名称不能为空");
        Validate.notBlank(phone, "联系电话不能为空");

        Customer customer = new Customer();
        customer.id = UUID.randomUUID().toString();
        customer.name = name;
        customer.phone = phone;
        customer.type = type;
        customer.active = true;
        customer.createdAt = LocalDateTime.now();

        return customer;
    }

    public void deactivate() {
        this.active = false;
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-customer/
git commit -m "feat(customer): 实现客户聚合根"
```

---

### Task 23: 实现库存上下文 (Inventory BC)

**Files:**
- Create: `gh-transport-inventory/inventory-domain/src/main/java/com/ghtransport/inventory/domain/aggregate/Warehouse.java`
- Create: `gh-transport-inventory/inventory-domain/src/main/java/com/ghtransport/inventory/domain/aggregate/InventoryItem.java`

**Step 1: 创建库存聚合根**

```java
package com.ghtransport.inventory.domain.aggregate;

import com.ghtransport.common.domain.AggregateRoot;
import com.ghtransport.common.util.Validate;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class InventoryItem extends AggregateRoot<String> {

    private String sku;
    private String name;
    private BigDecimal quantity;
    private String warehouseId;
    private BigDecimal reservedQuantity;
    private LocalDateTime lastRestockDate;

    public static InventoryItem create(String sku, String name, BigDecimal quantity, String warehouseId) {
        Validate.notBlank(sku, "SKU不能为空");
        Validate.notBlank(name, "商品名称不能为空");

        InventoryItem item = new InventoryItem();
        item.id = UUID.randomUUID().toString();
        item.sku = sku;
        item.name = name;
        item.quantity = quantity;
        item.warehouseId = warehouseId;
        item.reservedQuantity = BigDecimal.ZERO;

        return item;
    }

    public void reserve(BigDecimal amount) {
        if (quantity.subtract(reservedQuantity).compareTo(amount) < 0) {
            throw new RuntimeException("库存不足");
        }
        this.reservedQuantity = this.reservedQuantity.add(amount);
    }

    public void release(BigDecimal amount) {
        this.reservedQuantity = this.reservedQuantity.subtract(amount);
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-inventory/
git commit -m "feat(inventory): 实现库存聚合根"
```

---

### Task 24: 实现系统管理上下文 (System BC)

**Files:**
- Create: `gh-transport-system/system-domain/src/main/java/com/ghtransport/system/domain/aggregate/User.java`
- Create: `gh-transport-system/system-domain/src/main/java/com/ghtransport/system/domain/aggregate/Role.java`

**Step 1: 创建用户聚合根**

```java
package com.ghtransport.system.domain.aggregate;

import com.ghtransport.common.domain.AggregateRoot;
import com.ghtransport.common.util.Validate;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class User extends AggregateRoot<String> {

    private String username;
    private String passwordHash;
    private String email;
    private String phone;
    private Set<String> roleIds;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static User create(String username, String passwordHash, String email) {
        Validate.notBlank(username, "用户名不能为空");
        Validate.notBlank(passwordHash, "密码不能为空");

        User user = new User();
        user.id = UUID.randomUUID().toString();
        user.username = username;
        user.passwordHash = passwordHash;
        user.email = email;
        user.roleIds = new HashSet<>();
        user.enabled = true;
        user.createdAt = LocalDateTime.now();

        return user;
    }

    public void assignRole(String roleId) {
        this.roleIds.add(roleId);
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-system/
git commit -m "feat(system): 实现用户聚合根"
```

---

### Task 25: 实现API网关

**Files:**
- Create: `gh-transport-gateway/pom.xml`
- Create: `gh-transport-gateway/src/main/java/com/ghtransport/gateway/GatewayApplication.java`
- Create: `gh-transport-gateway/src/main/resources/application.yml`

**Step 1: 创建网关应用主类**

```java
package com.ghtransport.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

**Step 2: 创建网关配置**

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/v1/orders/**
        - id: dispatch-service
          uri: lb://dispatch-service
          predicates:
            - Path=/api/v1/dispatches/**
        - id: transport-service
          uri: lb://transport-service
          predicates:
            - Path=/api/v1/vehicles/**,/api/v1/drivers/**
```

**Step 3: Commit**

```bash
git add gh-transport-gateway/
git commit -m "feat(gateway): 实现API网关"
```

---

## Phase 4: 测试与文档

### Task 26: 单元测试

**Files:**
- Create: `order-domain/src/test/java/com/ghtransport/order/domain/aggregate/OrderTest.java`
- Create: `order-domain/src/test/java/com/ghtransport/order/domain/vo/MoneyTest.java`
- Create: `order-application/src/test/java/com/ghtransport/order/application/service/OrderApplicationServiceTest.java`

**Step 1: 创建订单聚合根单元测试**

```java
package com.ghtransport.order.domain.aggregate;

import com.ghtransport.order.domain.entity.OrderItem;
import com.ghtransport.order.domain.vo.Address;
import com.ghtransport.order.domain.vo.Money;
import com.ghtransport.order.domain.vo.Quantity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void should_create_order_success() {
        // Given
        String customerId = "customer-123";
        Address pickupAddress = new Address("广东", "深圳", "南山", "科技园", "张三", "13800138000");
        Address deliveryAddress = new Address("广东", "广州", "天河", "体育西路", "李四", "13900139000");

        OrderItem item = OrderItem.create(
            "电子产品",
            new Quantity(10),
            new BigDecimal("2.5"),
            new BigDecimal("0.1"),
            new Money(new BigDecimal("100"))
        );

        // When
        Order order = Order.create(
            customerId,
            List.of(item),
            pickupAddress,
            deliveryAddress,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1)
        );

        // Then
        assertNotNull(order.getId());
        assertNotNull(order.getOrderNo());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertFalse(order.getDomainEvents().isEmpty());
    }

    @Test
    void should_cancel_order_success_when_pending() {
        // Given
        Order order = createTestOrder();

        // When
        order.cancel("客户取消");

        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertFalse(order.getDomainEvents().isEmpty());
    }

    @Test
    void should_fail_cancel_order_when_not_pending() {
        // Given
        Order order = createTestOrder();
        order.markDispatched();

        // When & Then
        assertThrows(DomainException.class, () -> order.cancel("测试取消"));
    }
}
```

**Step 2: Commit**

```bash
git add order-domain/src/test/
git add order-application/src/test/
git commit -m "test: 添加订单领域模型单元测试"
```

---

### Task 27: 集成测试

**Files:**
- Create: `order-infrastructure/src/test/java/com/ghtransport/order/infrastructure/repository/OrderRepositoryImplTest.java`
- Create: `order-interfaces/src/test/java/com/ghtransport/order/interfaces/controller/OrderControllerTest.java`

**Step 1: 创建仓储集成测试**

```java
package com.ghtransport.order.infrastructure.repository;

import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.vo.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryImplTest {

    @Autowired
    private OrderRepositoryImpl orderRepository;

    @Test
    void should_save_and_find_order() {
        // Given
        Order order = createTestOrder();

        // When
        orderRepository.save(order);
        Order found = orderRepository.findById(order.getId());

        // Then
        assertNotNull(found);
        assertEquals(order.getOrderNo(), found.getOrderNo());
    }

    @Test
    void should_find_orders_by_status() {
        // Given
        Order order = createTestOrder();
        orderRepository.save(order);

        // When
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        // Then
        assertFalse(pendingOrders.isEmpty());
        assertTrue(pendingOrders.stream()
            .allMatch(o -> o.getStatus() == OrderStatus.PENDING));
    }
}
```

**Step 2: Commit**

```bash
git add order-infrastructure/src/test/
git add order-interfaces/src/test/
git commit -m "test: 添加集成测试"
```

---

### Task 28: 配置文件汇总

**Files:**
- Create: `order-infrastructure/src/main/resources/application.yml`
- Create: `order-interfaces/src/main/resources/application.yml`
- Create: `gh-transport-common/src/main/resources/application.yml`

**Step 1: 创建订单服务配置**

```yaml
server:
  port: 8081

spring:
  application:
    name: order-service
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/gh_transport?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    serialization:
      write-dates-as-timestamps: false

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.ghtransport.order.infrastructure.po
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    com.ghtransport: debug
    com.ghtransport.order: debug
```

**Step 2: Commit**

```bash
git add */src/main/resources/application.yml
git commit -m "chore: 添加配置文件"
```

---

### Task 29: README文档

**Files:**
- Create: `README.md`
- Create: `docs/ARCHITECTURE.md`

**Step 1: 创建项目README**

```markdown
# GH Transport - 物流ERP系统

## 项目简介
基于DDD领域驱动设计的物流ERP系统，支持订单管理、调度管理、运输管理等核心业务。

## 技术栈
- Java 17 / Spring Boot 3.2
- MyBatis-Plus 3.5
- DDD四层架构
- CQRS模式

## 项目结构

```
gh-transport-parent/
├── gh-transport-common/           # 共享领域基类
├── gh-transport-order/            # 订单限界上下文
├── gh-transport-dispatch/         # 调度限界上下文
├── gh-transport-transport/        # 运输限界上下文
├── gh-transport-customer/         # 客户限界上下文
├── gh-transport-inventory/        # 库存限界上下文
├── gh-transport-auth/             # 认证限界上下文
├── gh-transport-system/           # 系统管理限界上下文
└── gh-transport-gateway/          # API网关
```

## 快速开始

```bash
# 编译项目
mvn clean install -DskipTests

# 运行订单服务
cd gh-transport-order/order-interfaces
mvn spring-boot:run
```

## API文档

- 订单服务: http://localhost:8081/swagger-ui.html
```

**Step 2: Commit**

```bash
git add README.md docs/
git commit -m "docs: 添加项目文档"
```

---

## 总结

**DDD实施完成标准：**
- [ ] 四层架构完整 (Task 1-12)
- [ ] 充血模型实现 (Task 3, 20-25)
- [ ] 领域事件机制 (Task 7)
- [ ] CQRS读写分离 (Task 8)
- [ ] 防腐层(ACL) (Task 9)
- [ ] 聚合根边界清晰
- [ ] 领域服务与规格模式 (Task 13)
- [ ] Saga流程管理器 (Task 14)
- [ ] 多租户支持 (Task 15)
- [ ] 事件溯源 (Task 18)
- [ ] ID生成策略 (Task 16)
- [ ] 异常层次结构 (Task 19)
- [ ] 单元测试 (Task 26)
- [ ] 集成测试 (Task 27)

**计划完成并保存到 `docs/plans/2025-01-27-logistics-erp-ddd-implementation.md`**

**执行选项：**
1. **Subagent-Driven** - 逐个Task执行
2. **Parallel Session** - 批量执行

**请选择执行方式？**
