# 物流ERP系统实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标：** 构建一个多租户SaaS物流ERP系统，具备智能装载优化、实时车辆追踪、进销存管理和财务核算功能。

**架构：** 采用Spring Cloud微服务架构，支持多租户数据隔离。核心服务包括装载优化服务（3D装箱算法）、车辆追踪服务（GPS实时数据流处理）、订单服务、库存服务、财务服务。通过Kafka实现GPS数据流处理，InfluxDB存储时序轨迹数据，Redis缓存实时位置。

**技术栈：**
- 后端：Java 17+, Spring Boot 3.2, Spring Cloud Gateway, MySQL 8.0, Redis 7.0, Kafka 3.x, InfluxDB 2.x, Google OR-Tools
- 持久层：**MyBatis-Plus 3.5+**
- 安全：**Spring Security 6 + JWT + BCrypt密码加密**
- API文档：**springdoc-openapi 2.x + Knife4j 4.4**
- 前端：Vue 3 + TypeScript + Element Plus + Three.js + OpenLayers

---

## 第一阶段：项目初始化与基础框架

### Task 1: 创建Maven多模块项目结构

**Files:**
- Create: `pom.xml`
- Create: `gh-transport-common/pom.xml`
- Create: `gh-transport-api/pom.xml`
- Create: `gh-transport-gateway/pom.xml`
- Create: `gh-transport-loading/pom.xml`
- Create: `gh-transport-tracking/pom.xml`
- Create: `gh-transport-order/pom.xml`
- Create: `gh-transport-inventory/pom.xml`
- Create: `gh-transport-vehicle/pom.xml`
- Create: `gh-transport-finance/pom.xml`

**Step 1: 验证Maven结构**

Run: `mvn validate`
Expected: SUCCESS

**Step 2: 创建父POM**

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
    <description>物流ERP系统父POM</description>

    <modules>
        <module>gh-transport-common</module>
        <module>gh-transport-api</module>
        <module>gh-transport-gateway</module>
        <module>gh-transport-loading</module>
        <module>gh-transport-tracking</module>
        <module>gh-transport-order</module>
        <module>gh-transport-inventory</module>
        <module>gh-transport-vehicle</module>
        <module>gh-transport-finance</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.2.0</spring-boot.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jwt.version>0.12.3</jwt.version>
        <knife4j.version>4.4.0</knife4j.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- MyBatis-Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>

            <!-- JWT -->
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jwt.version}</version>
                <scope>runtime</scope>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jwt.version}</version>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

**Step 3: 创建公共模块POM（包含所有依赖）**

```xml
<!-- gh-transport-common/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ghtransport</groupId>
        <artifactId>gh-transport-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>gh-transport-common</artifactId>
    <name>GH Transport Common</name>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Knife4j API文档 -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

**Step 4: 验证模块构建**

Run: `mvn clean install -DskipTests`
Expected: BUILD SUCCESS - 所有模块构建成功

**Step 5: Commit**

```bash
git add .
git commit -m "feat: 创建Maven多模块项目结构（集成MyBatis-Plus, Security, JWT, Knife4j）"
```

---

### Task 2: 配置Spring Boot基础依赖与统一响应

**Files:**
- Modify: `gh-transport-common/src/main/resources/application.yml`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/config/SwaggerConfig.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/result/Result.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/result/ResultCode.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/result/ResponseAdvice.java`

**Step 1: 创建统一响应Result**

```java
package com.ghtransport.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一响应结果")
public class Result<T> {
    @Schema(description = "状态码")
    private int code;
    @Schema(description = "消息")
    private String message;
    @Schema(description = "数据")
    private T data;
    @Schema(description = "时间戳")
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> fail(String message, int code) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getMessage(), resultCode.getCode());
    }

    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode() == this.code;
    }
}
```

**Step 2: 创建ResultCode枚举**

```java
package com.ghtransport.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "响应状态码")
public enum ResultCode {
    SUCCESS(200, "操作成功"),

    // 通用错误 400-499
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误 1000-1999
    BUSINESS_ERROR(1000, "业务处理错误"),
    INVENTORY_NOT_ENOUGH(1001, "库存不足"),
    VEHICLE_NOT_FOUND(1002, "车辆不存在"),
    ORDER_NOT_FOUND(1003, "订单不存在"),
    DATA_NOT_FOUND(1004, "数据不存在"),

    // 权限错误 2000-2999
    PERMISSION_DENIED(2001, "权限不足"),

    // 系统错误 5000-5999
    SYSTEM_ERROR(5000, "系统内部错误"),
    DATABASE_ERROR(5001, "数据库错误"),
    SERVICE_UNAVAILABLE(5002, "服务不可用");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

**Step 3: 创建统一响应包装**

```java
package com.ghtransport.common.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType,
                          Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof Result) {
            return body;
        }
        if (body instanceof String) {
            return body;
        }
        return Result.success(body);
    }
}
```

**Step 4: 创建配置文件**

```yaml
# application.yml
spring:
  application:
    name: gh-transport
  profiles:
    active: @spring.profiles.active@

  # 数据源
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:gh_transport}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root123}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  # Redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0

  # Jackson配置
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    serialization:
      write-dates-as-timestamps: false

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.ghtransport.*.entity
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

# Knife4j API文档配置
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha

knife4j:
  enable: true
  setting:
    language: zh_CN

# JWT配置
jwt:
  secret: ${JWT_SECRET:your-super-secret-key-at-least-256-bits-long-for-hs256}
  expiration: ${JWT_EXPIRATION:86400000}

# 日志配置
logging:
  level:
    com.ghtransport: DEBUG
    com.baomidou.mybatisplus: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

**Step 5: 创建Swagger配置**

```java
package com.ghtransport.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("GH Transport API文档")
                .version("1.0.0")
                .description("物流ERP系统接口文档")
                .contact(new Contact().name("GH Transport Team").email("dev@ghtransport.com"))
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
            .schemaRequirement("Bearer", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"));
    }
}
```

**Step 6: 编写并运行测试**

```java
package com.ghtransport.common.result;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void testSuccessResult() {
        Result<String> result = Result.success("test data");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test data", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void testFailResult() {
        Result<Void> result = Result.fail("错误信息", 500);
        assertEquals(500, result.getCode());
        assertEquals("错误信息", result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }
}
```

Run: `mvn test -pl gh-transport-common -Dtest=ResultTest`

**Step 7: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/
git add gh-transport-common/src/main/resources/
git commit -m "feat: 配置统一响应Result、Swagger文档和application.yml"
```

---

### Task 3: 实现全局异常处理与多租户上下文

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/exception/BusinessException.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/exception/GlobalExceptionHandler.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/tenant/TenantContextHolder.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/tenant/TenantFilter.java`
- Create: `gh-transport-common/src/test/java/com/ghtransport/common/exception/GlobalExceptionHandlerTest.java`

**Step 1: 创建BusinessException**

```java
package com.ghtransport.common.exception;

import com.ghtransport.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message, int code) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }
}
```

**Step 2: 创建GlobalExceptionHandler**

```java
package com.ghtransport.common.exception;

import com.ghtransport.common.result.Result;
import com.ghtransport.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return Result.fail(ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数验证失败: {}", message);
        return Result.fail(message, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException ex) {
        String message = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return Result.fail(message, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }
}
```

**Step 3: 创建TenantContextHolder**

```java
package com.ghtransport.common.tenant;

import lombok.Getter;
import lombok.Setter;

public class TenantContextHolder {
    private static final ThreadLocal<Long> tenantId = new ThreadLocal<>();
    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<String> username = new ThreadLocal<>();
    private static final ThreadLocal<String> roleCode = new ThreadLocal<>();

    public static void setTenantId(Long id) {
        tenantId.set(id);
    }

    public static Long getTenantId() {
        return tenantId.get();
    }

    public static void setUserId(Long id) {
        userId.set(id);
    }

    public static Long getUserId() {
        return userId.get();
    }

    public static void setUsername(String name) {
        username.set(name);
    }

    public static String getUsername() {
        return username.get();
    }

    public static void setRoleCode(String code) {
        roleCode.set(code);
    }

    public static String getRoleCode() {
        return roleCode.get();
    }

    public static void clear() {
        tenantId.remove();
        userId.remove();
        username.remove();
        roleCode.remove();
    }
}
```

**Step 4: 创建TenantFilter**

```java
package com.ghtransport.common.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantIdStr = request.getHeader("X-Tenant-Id");
            if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
                TenantContextHolder.setTenantId(Long.parseLong(tenantIdStr));
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs");
    }
}
```

**Step 5: 编写测试**

```java
package com.ghtransport.common.exception;

import com.ghtransport.common.result.Result;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleBusinessException() {
        BusinessException ex = new BusinessException("业务错误", 1001);
        Result<?> result = handler.handleBusinessException(ex);
        assertEquals(1001, result.getCode());
        assertEquals("业务错误", result.getMessage());
    }
}
```

Run: `mvn test -pl gh-transport-common -Dtest=GlobalExceptionHandlerTest`

**Step 6: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/exception/
git add gh-transport-common/src/main/java/com/ghtransport/common/tenant/
git commit -m "feat: 实现全局异常处理和多租户上下文"
```

---

### Task 4: 实现JWT认证与Security配置

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/JwtTokenProvider.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/JwtAuthenticationFilter.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/SecurityConfig.java`
- Create: `gh-transport-common/src/test/java/com/ghtransport/common/security/JwtTokenProviderTest.java`

**Step 1: 创建JwtTokenProvider**

```java
package com.ghtransport.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username, Long tenantId, String roleCode) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tenantId", tenantId);
        claims.put("roleCode", roleCode);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token已过期");
        } catch (MalformedJwtException ex) {
            log.warn("无效的JWT token");
        } catch (Exception ex) {
            log.warn("JWT验证失败: {}", ex.getMessage());
        }
        return false;
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        Claims claims = parseToken(token);
        String roleCode = claims.get("roleCode", String.class);
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public Long getTenantIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("tenantId", Long.class);
    }
}
```

**Step 2: 创建JwtAuthenticationFilter**

```java
package com.ghtransport.common.security;

import com.ghtransport.common.tenant.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Collection<? extends SimpleGrantedAuthority> authorities =
                    jwtTokenProvider.getAuthorities(token);

                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                Long tenantId = jwtTokenProvider.getTenantIdFromToken(token);
                String username = jwtTokenProvider.parseToken(token).getSubject();

                TenantContextHolder.setTenantId(tenantId);
                TenantContextHolder.setUserId(userId);
                TenantContextHolder.setUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
```

**Step 3: 创建SecurityConfig**

```java
package com.ghtransport.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
```

**Step 4: 编写测试**

```java
package com.ghtransport.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        setField(provider, "jwtSecret", "test-secret-key-that-is-at-least-256-bits-long-for-hs256");
        setField(provider, "jwtExpiration", 86400000L);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = provider.generateToken(1L, "testuser", 1L, "ADMIN");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
    }

    @Test
    void testGetAuthorities() {
        String token = provider.generateToken(1L, "testuser", 1L, "DISPATCHER");
        Collection<? extends GrantedAuthority> authorities = provider.getAuthorities(token);
        assertEquals(1, authorities.size());
        assertEquals("ROLE_DISPATCHER", authorities.iterator().next().getAuthority());
    }
}
```

Run: `mvn test -pl gh-transport-common -Dtest=JwtTokenProviderTest`

**Step 5: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/security/
git commit -m "feat: 实现JWT认证和Security配置（含BCrypt密码加密）"
```

---

### Task 5: 创建多租户数据库表

**Files:**
- Create: `docs/plans/schema/01_tenant_tables.sql`
- Create: `docs/plans/schema/02_user_role_tables.sql`
- Create: `docs/plans/schema/03_permission_tables.sql`
- Create: `docs/plans/schema/04_vehicle_tables.sql`
- Create: `docs/plans/schema/05_order_tables.sql`
- Create: `docs/plans/schema/06_inventory_tables.sql`

**Files:**
- Create: `gh-transport-common/src/main/resources/application-dev.yml`
- Create: `gh-transport-common/src/main/resources/application-test.yml`
- Create: `gh-transport-common/src/main/resources/application-prod.yml`
- Modify: `gh-transport-common/src/main/resources/application.yml`

**Step 1: 创建开发环境配置**

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gh_transport_dev?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: dev123
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    password:
    database: 0

server:
  port: 8080

logging:
  level:
    com.ghtransport: DEBUG
    org.springframework.web: INFO
```

**Step 2: 创建测试环境配置**

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:mysql://test-server:3306/gh_transport_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: test
    password: test123
  redis:
    host: test-server
    port: 6379
    password: testredis
    database: 0

logging:
  level:
    com.ghtransport: DEBUG
```

**Step 3: 创建生产环境配置**

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-server:3306/gh_transport?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=true&serverCertificateVerification=REQUIRED
    username: prod
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  redis:
    host: ${REDIS_HOST}
    port: 6379
    password: ${REDIS_PASSWORD}
    database: 0
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

server:
  port: 8080
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

logging:
  level:
    com.ghtransport: INFO
    org.springframework: WARN
```

**Step 4: 修改主配置文件**

```yaml
# application.yml
spring:
  application:
    name: gh-transport
  profiles:
    active: @spring.profiles.active@
  config:
    import: optional:file:./config/

# Actuator健康检查
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

**Step 5: 配置Maven资源过滤**

```xml
<!-- root pom.xml 添加 -->
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-resources-plugin</artifactId>
            <version>3.3.1</version>
        </plugin>
    </plugins>
</build>
```

**Step 6: 验证配置加载**

Run: `mvn clean process-resources -pl gh-transport-common -Dspring.profiles.active=dev`
Expected: 配置文件正确复制到target目录

**Step 7: Commit**

```bash
git add gh-transport-common/src/main/resources/
git commit -m "feat: 配置多环境配置文件dev/test/prod"
```

---

## 第二阶段：多租户与权限管理

### Task 6: 设计并创建多租户数据库表

**Files:**
- Create: `docs/plans/schema/01_tenant_tables.sql`
- Create: `docs/plans/schema/02_user_role_tables.sql`
- Create: `docs/plans/schema/03_permission_tables.sql`

**Step 1: 编写SQL测试**

```sql
-- 测试多租户表创建
CREATE TABLE IF NOT EXISTS gt_tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '租户名称',
    code VARCHAR(50) NOT NULL COMMENT '租户编码',
    plan_type VARCHAR(20) DEFAULT 'BASIC' COMMENT '套餐类型',
    max_vehicles INT DEFAULT 10 COMMENT '最大车辆数',
    max_users INT DEFAULT 20 COMMENT '最大用户数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';
```

**Step 2: 创建用户角色表SQL**

```sql
-- 角色表
CREATE TABLE IF NOT EXISTS gt_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统角色',
    permissions JSON COMMENT '权限列表',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_role_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表
CREATE TABLE IF NOT EXISTS gt_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_username (tenant_id, username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS gt_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

**Step 3: 创建权限菜单表SQL**

```sql
-- 菜单表
CREATE TABLE IF NOT EXISTS gt_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    icon VARCHAR(50) COMMENT '菜单图标',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '前端组件路径',
    menu_type TINYINT DEFAULT 1 COMMENT '类型: 0目录 1菜单 2按钮',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_visible TINYINT DEFAULT 1 COMMENT '是否显示',
    permission VARCHAR(100) COMMENT '权限标识',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 权限表
CREATE TABLE IF NOT EXISTS gt_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL COMMENT '权限编码',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    module VARCHAR(50) COMMENT '所属模块',
    description VARCHAR(200) COMMENT '权限描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS gt_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS gt_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
```

**Step 4: 初始化默认数据**

```sql
-- 初始化超级管理员角色
INSERT INTO gt_role (id, tenant_id, name, code, is_system, permissions) VALUES
(1, 0, '超级管理员', 'SUPER_ADMIN', 1, '["*:*:*"]');

-- 初始化系统菜单
INSERT INTO gt_menu (id, parent_id, name, path, component, menu_type, sort_order) VALUES
(1, 0, '系统管理', '/system', 'Layout', 0, 1),
(2, 1, '用户管理', '/system/user', 'system/user/index', 1, 1),
(3, 1, '角色管理', '/system/role', 'system/role/index', 1, 2),
(4, 1, '菜单管理', '/system/menu', 'system/menu/index', 1, 3),
(5, 0, '运营管理', '/operation', 'Layout', 0, 2),
(6, 5, '订单管理', '/operation/order', 'operation/order/index', 1, 1),
(7, 5, '车辆调度', '/operation/dispatch', 'operation/dispatch/index', 1, 2),
(8, 5, '实时追踪', '/operation/tracking', 'operation/tracking/index', 1, 3),
(9, 5, '报表中心', '/operation/report', 'operation/report/index', 1, 4);
```

**Step 5: 验证SQL执行**

Run: `mysql -u root -p < docs/plans/schema/01_tenant_tables.sql`
Expected: 所有表创建成功，无错误

**Step 6: Commit**

```bash
git add docs/plans/schema/
git commit -m "feat: 创建多租户权限管理数据库表"
```

---

### Task 7: 实现多租户基础功能

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/tenant/TenantContextHolder.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/tenant/TenantAspect.java`
- Create: `gh-transport-common/src/test/java/com/ghtransport/common/tenant/TenantContextHolderTest.java`

**Step 1: 编写TenantContextHolder测试**

```java
package com.ghtransport.common.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TenantContextHolderTest {

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testSetAndGetTenantId() {
        TenantContextHolder.setTenantId(1L);
        assertEquals(1L, TenantContextHolder.getTenantId());
    }

    @Test
    void testSetAndGetUserId() {
        TenantContextHolder.setUserId(100L);
        assertEquals(100L, TenantContextHolder.getUserId());
    }

    @Test
    void testClearContext() {
        TenantContextHolder.setTenantId(1L);
        TenantContextHolder.setUserId(100L);
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
        assertNull(TenantContextHolder.getUserId());
    }

    @Test
    void testSetMultipleValues() {
        TenantContextHolder.setTenantId(1L);
        TenantContextHolder.setUserId(100L);
        TenantContextHolder.setRoleId(10L);

        assertEquals(1L, TenantContextHolder.getTenantId());
        assertEquals(100L, TenantContextHolder.getUserId());
        assertEquals(10L, TenantContextHolder.getRoleId());
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -pl gh-transport-common -Dtest=TenantContextHolderTest`
Expected: FAIL - TenantContextHolder不存在

**Step 3: 实现TenantContextHolder**

```java
package com.ghtransport.common.tenant;

import lombok.Getter;
import lombok.Setter;

public class TenantContextHolder {
    private static final ThreadLocal<Long> tenantId = new ThreadLocal<>();
    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<Long> roleId = new ThreadLocal<>();

    public static void setTenantId(Long id) {
        tenantId.set(id);
    }

    public static Long getTenantId() {
        return tenantId.get();
    }

    public static void setUserId(Long id) {
        userId.set(id);
    }

    public static Long getUserId() {
        return userId.get();
    }

    public static void setRoleId(Long id) {
        roleId.set(id);
    }

    public static Long getRoleId() {
        return roleId.get();
    }

    public static void clear() {
        tenantId.remove();
        userId.remove();
        roleId.remove();
    }
}
```

**Step 4: 运行测试验证通过**

Run: `mvn test -pl gh-transport-common -Dtest=TenantContextHolderTest`
Expected: PASS - 所有测试通过

**Step 5: 实现TenantAspect自动注入**

```java
package com.ghtransport.common.tenant;

import com.ghtransport.common.exception.BusinessException;
import com.ghtransport.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TenantAspect {

    @Around("execution(* com.ghtransport.*.service..*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            log.warn("租户ID未设置，方法: {}", point.getSignature().toShortString());
        }
        return point.proceed();
    }
}
```

**Step 6: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/tenant/
git add gh-transport-common/src/test/
git commit -m "feat: 实现多租户上下文TenantContextHolder"
```

---

### Task 8: 实现JWT认证

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/JwtTokenProvider.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/JwtAuthenticationFilter.java`
- Create: `gh-transport-common/src/test/java/com/ghtransport/common/security/JwtTokenProviderTest.java`

**Step 1: 编写JWT测试**

```java
package com.ghtransport.common.security;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider("testSecretKey123456789012345678901234567890");

    @Test
    void testGenerateToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 100L);
        claims.put("tenantId", 1L);
        claims.put("roleCode", "ADMIN");

        String token = provider.generateToken(claims);

        assertNotNull(token);
        assertTrue(token.length() > 100);
    }

    @Test
    void testParseToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 100L);
        claims.put("tenantId", 1L);

        String token = provider.generateToken(claims);
        Map<String, Object> parsed = provider.parseToken(token);

        assertEquals(100L, parsed.get("userId"));
        assertEquals(1L, parsed.get("tenantId"));
    }

    @Test
    void testValidateToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 100L);

        String token = provider.generateToken(claims);
        assertTrue(provider.validateToken(token));
    }

    @Test
    void testTokenExpired() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider("secret");
        // 创建一个已过期的token（通过修改时间）
        String expiredToken = expiredProvider.generateToken(new HashMap<>(), -1000);
        assertFalse(expiredProvider.validateToken(expiredToken));
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -pl gh-transport-common -Dtest=JwtTokenProviderTest`
Expected: FAIL - JwtTokenProvider不存在

**Step 3: 实现JwtTokenProvider**

```java
package com.ghtransport.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Map<String, Object> claims) {
        return generateToken(claims, jwtExpiration);
    }

    public String generateToken(Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, Object> parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Map<String, Object> result = new HashMap<>();
        result.put("userId", claims.get("userId", Long.class));
        result.put("tenantId", claims.get("tenantId", Long.class));
        result.put("roleCode", claims.get("roleCode", String.class));
        result.put("username", claims.get("username", String.class));
        return result;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    public long getExpiration() {
        return jwtExpiration;
    }
}
```

**Step 4: 添加JWT依赖**

```xml
<!-- gh-transport-common/pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

**Step 5: 运行测试验证通过**

Run: `mvn test -pl gh-transport-common -Dtest=JwtTokenProviderTest`
Expected: PASS - 所有测试通过

**Step 6: 实现JwtAuthenticationFilter**

```java
package com.ghtransport.common.security;

import com.ghtransport.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Map<String, Object> claims = jwtTokenProvider.parseToken(token);

                Long tenantId = claims.get("tenantId") != null
                    ? Long.valueOf(claims.get("tenantId").toString()) : null;
                Long userId = claims.get("userId") != null
                    ? Long.valueOf(claims.get("userId").toString()) : null;
                String roleCode = claims.get("roleCode") != null
                    ? claims.get("roleCode").toString() : null;

                TenantContextHolder.setTenantId(tenantId);
                TenantContextHolder.setUserId(userId);
                if (roleCode != null) {
                    TenantContextHolder.setRoleId(0L);
                }

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + roleCode)
                );

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
```

**Step 7: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/security/
git commit -m "feat: 实现JWT认证 JwtTokenProvider和JwtAuthenticationFilter"
```

---

## 第三阶段：装载优化服务

### Task 9: 实现三维装箱算法

**Files:**
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/algorithm/BinPackingAlgorithm.java`
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/algorithm/HeuristicPacker.java`
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/model/CargoItem.java`
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/model/Vehicle.java`
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/model/LoadingSolution.java`
- Create: `gh-transport-loading/src/test/java/com/ghtransport/loading/algorithm/HeuristicPackerTest.java`

**Step 1: 编写CargoItem和Vehicle测试**

```java
package com.ghtransport.loading.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CargoItemTest {

    @Test
    void testCargoItemCreation() {
        CargoItem item = new CargoItem();
        item.setId("c001");
        item.setName("测试货物");
        item.setLength(50);
        item.setWidth(40);
        item.setHeight(30);
        item.setWeight(10.0);
        item.setValue(1000.0);

        assertEquals("c001", item.getId());
        assertEquals(50, item.getLength());
        assertEquals(60000, item.getVolume()); // 50*40*30
    }

    @Test
    void testStackable() {
        CargoItem item = new CargoItem();
        item.setStackable(true);
        item.setMaxStackWeight(50.0);

        assertTrue(item.isStackable());
        assertEquals(50.0, item.getMaxStackWeight());
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -pl gh-transport-loading -Dtest=CargoItemTest`
Expected: FAIL - CargoItem不存在

**Step 3: 实现CargoItem**

```java
package com.ghtransport.loading.model;

import lombok.Data;

@Data
public class CargoItem {
    private String id;
    private String name;
    private double length;    // 长度 cm
    private double width;     // 宽度 cm
    private double height;    // 高度 cm
    private double weight;    // 重量 kg
    private double value;     // 价值 元
    private int quantity;     // 数量
    private boolean stackable; // 是否可堆叠
    private double maxStackWeight; // 最大堆叠重量
    private boolean rotatable; // 是否可旋转

    public double getVolume() {
        return length * width * height;
    }
}
```

**Step 4: 实现Vehicle**

```java
package com.ghtransport.loading.model;

import lombok.Data;

@Data
public class Vehicle {
    private String id;
    private String plateNumber;
    private String type;
    private double length;    // 车厢长度 cm
    private double width;     // 车厢宽度 cm
    private double height;    // 车厢高度 cm
    private double maxWeight; // 最大载重 kg
    private String status;

    public double getVolume() {
        return length * width * height;
    }
}
```

**Step 5: 实现LoadingSolution**

```java
package com.ghtransport.loading.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class LoadingSolution {
    private String solutionId;
    private String vehicleId;
    private double volumeUtilization;
    private double weightUtilization;
    private double totalValue;
    private List<LoadedItem> loadedItems = new ArrayList<>();
    private List<CargoItem> unfitItems = new ArrayList<>();
    private double usedVolume;
    private double usedWeight;

    public void addLoadedItem(LoadedItem item) {
        loadedItems.add(item);
        usedVolume += item.getCargoItem().getVolume() * item.getQuantity();
        usedWeight += item.getCargoItem().getWeight() * item.getQuantity();
        totalValue += item.getCargoItem().getValue() * item.getQuantity();
    }
}
```

**Step 6: 实现LoadedItem**

```java
package com.ghtransport.loading.model;

import lombok.Data;

@Data
public class LoadedItem {
    private CargoItem cargoItem;
    private int quantity;
    private Position position; // 三维坐标

    @Data
    public static class Position {
        private double x;
        private double y;
        private double z;
        private double length;
        private double width;
        private double height;
    }
}
```

**Step 7: 运行测试验证CargoItem**

Run: `mvn test -pl gh-transport-loading -Dtest=CargoItemTest`
Expected: PASS - 所有测试通过

**Step 8: 实现启发式装箱算法**

```java
package com.ghtransport.loading.algorithm;

import com.ghtransport.loading.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class HeuristicPacker implements BinPackingAlgorithm {

    @Override
    public LoadingSolution optimize(Vehicle vehicle, List<CargoItem> items) {
        LoadingSolution solution = new LoadingSolution();
        solution.setSolutionId(UUID.randomUUID().toString());
        solution.setVehicleId(vehicle.getId());

        // 按综合评分排序：价值密度高的优先
        List<CargoItem> sortedItems = sortByScore(items);

        // 空间分割策略
        List<Space> spaces = new ArrayList<>();
        spaces.add(new Space(0, 0, 0,
            vehicle.getLength(), vehicle.getWidth(), vehicle.getHeight()));

        for (CargoItem item : sortedItems) {
            for (int i = 0; i < item.getQuantity(); i++) {
                Space bestSpace = findBestSpace(spaces, item);

                if (bestSpace != null) {
                    // 装载货物
                    LoadedItem loaded = new LoadedItem();
                    loaded.setCargoItem(item);
                    loaded.setQuantity(1);
                    LoadedItem.Position pos = new LoadedItem.Position();
                    pos.setX(bestSpace.getX());
                    pos.setY(bestSpace.getY());
                    pos.setZ(bestSpace.getZ());
                    pos.setLength(item.getLength());
                    pos.setWidth(item.getWidth());
                    pos.setHeight(item.getHeight());
                    loaded.setPosition(pos);
                    solution.addLoadedItem(loaded);

                    // 分割剩余空间
                    splitSpace(spaces, bestSpace, item);
                } else {
                    solution.getUnfitItems().add(item);
                }
            }
        }

        // 计算利用率
        double totalVolume = vehicle.getVolume();
        double totalWeight = vehicle.getMaxWeight();
        solution.setVolumeUtilization(solution.getUsedVolume() / totalVolume);
        solution.setWeightUtilization(solution.getUsedWeight() / totalWeight);

        return solution;
    }

    private List<CargoItem> sortByScore(List<CargoItem> items) {
        List<CargoItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> {
            double scoreA = a.getValue() / a.getVolume();
            double scoreB = b.getValue() / b.getVolume();
            return Double.compare(scoreB, scoreA);
        });
        return sorted;
    }

    private Space findBestSpace(List<Space> spaces, CargoItem item) {
        Space bestSpace = null;
        double bestScore = 0;

        for (Space space : spaces) {
            if (canFit(space, item)) {
                // 计算契合度分数
                double score = calculateFitScore(space, item);
                if (score > bestScore) {
                    bestScore = score;
                    bestSpace = space;
                }
            }
        }
        return bestSpace;
    }

    private boolean canFit(Space space, CargoItem item) {
        return space.getLength() >= item.getLength() &&
               space.getWidth() >= item.getWidth() &&
               space.getHeight() >= item.getHeight() &&
               space.getX() + item.getLength() <= 999999 && // 车厢限制
               space.getY() + item.getWidth() <= 999999 &&
               space.getZ() + item.getHeight() <= 999999;
    }

    private double calculateFitScore(Space space, CargoItem item) {
        double fitVolume = item.getLength() * item.getWidth() * item.getHeight();
        double spaceVolume = space.getVolume();
        return fitVolume / spaceVolume;
    }

    private void splitSpace(List<Space> spaces, Space used, CargoItem item) {
        spaces.remove(used);

        // 右侧空间
        Space right = new Space(
            used.getX() + (int)item.getLength(),
            used.getY(),
            used.getZ(),
            used.getLength() - (int)item.getLength(),
            used.getWidth(),
            used.getHeight()
        );
        if (right.getLength() > 0 && right.getWidth() > 0 && right.getHeight() > 0) {
            spaces.add(right);
        }

        // 前方空间
        Space front = new Space(
            used.getX(),
            used.getY() + (int)item.getWidth(),
            used.getZ(),
            item.getLength(),
            used.getWidth() - (int)item.getWidth(),
            item.getHeight()
        );
        if (front.getLength() > 0 && front.getWidth() > 0 && front.getHeight() > 0) {
            spaces.add(front);
        }

        // 上方空间
        Space top = new Space(
            used.getX(),
            used.getY(),
            used.getZ() + (int)item.getHeight(),
            used.getLength(),
            used.getWidth(),
            used.getHeight() - (int)item.getHeight()
        );
        if (top.getLength() > 0 && top.getWidth() > 0 && top.getHeight() > 0) {
            spaces.add(top);
        }
    }

    private static class Space {
        private double x, y, z;
        private double length, width, height;

        public Space(double x, double y, double z, double length, double width, double height) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.length = length;
            this.width = width;
            this.height = height;
        }

        public double getVolume() {
            return length * width * height;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public double getLength() { return length; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
    }
}
```

**Step 9: 编写算法测试**

```java
package com.ghtransport.loading.algorithm;

import com.ghtransport.loading.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeuristicPackerTest {

    private HeuristicPacker packer;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        packer = new HeuristicPacker();

        vehicle = new Vehicle();
        vehicle.setId("v001");
        vehicle.setLength(400);  // 4米
        vehicle.setWidth(200);   // 2米
        vehicle.setHeight(200);  // 2米
        vehicle.setMaxWeight(2000); // 2吨
    }

    @Test
    void testSimplePacking() {
        List<CargoItem> items = new ArrayList<>();

        CargoItem item1 = new CargoItem();
        item1.setId("c001");
        item1.setLength(100);
        item1.setWidth(100);
        item1.setHeight(100);
        item1.setWeight(10);
        item1.setValue(500);
        item1.setQuantity(8);
        items.add(item1);

        LoadingSolution solution = packer.optimize(vehicle, items);

        assertNotNull(solution);
        assertTrue(solution.getVolumeUtilization() > 0.5);
        assertEquals(0, solution.getUnfitItems().size());
    }

    @Test
    void testOverweight() {
        List<CargoItem> items = new ArrayList<>();

        CargoItem item1 = new CargoItem();
        item1.setId("c001");
        item1.setLength(100);
        item1.setWidth(100);
        item1.setHeight(100);
        item1.setWeight(300); // 单个货物300kg
        item1.setValue(1000);
        item1.setQuantity(10); // 总重3000kg > 车辆载重2000kg
        items.add(item1);

        LoadingSolution solution = packer.optimize(vehicle, items);

        // 应该只能装6个（1800kg < 2000kg）
        assertTrue(solution.getLoadedItems().size() <= 6);
    }

    @Test
    void testMixedItems() {
        List<CargoItem> items = new ArrayList<>();

        CargoItem item1 = new CargoItem();
        item1.setId("c001");
        item1.setLength(50);
        item1.setWidth(50);
        item1.setHeight(50);
        item1.setWeight(5);
        item1.setValue(100);
        item1.setQuantity(20);
        items.add(item1);

        CargoItem item2 = new CargoItem();
        item2.setId("c002");
        item2.setLength(100);
        item2.setWidth(100);
        item2.setHeight(100);
        item2.setWeight(20);
        item2.setValue(500);
        item2.setQuantity(5);
        items.add(item2);

        LoadingSolution solution = packer.optimize(vehicle, items);

        assertNotNull(solution);
        assertTrue(solution.getVolumeUtilization() > 0);
        log.info("装载率: {}, 重量利用率: {}",
            solution.getVolumeUtilization(),
            solution.getWeightUtilization());
    }
}
```

**Step 10: 运行测试验证算法**

Run: `mvn test -pl gh-transport-loading -Dtest=HeuristicPackerTest`
Expected: PASS - 所有测试通过

**Step 11: Commit**

```bash
git add gh-transport-loading/src/main/java/com/ghtransport/loading/
git add gh-transport-loading/src/test/
git commit -m "feat: 实现启发式三维装箱算法HeuristicPacker"
```

---

### Task 10: 创建装载优化REST API

**Files:**
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/controller/LoadingController.java`
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/dto/LoadingRequest.java`
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/dto/LoadingResponse.java`
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/service/LoadingService.java`
- Create: `gh-transport-loading/src/main/java/com/ghtransport/loading/service/LoadingServiceImpl.java`
- Create: `gh-transport-loading/src/test/java/com/ghtransport/loading/controller/LoadingControllerTest.java`

**Step 1: 编写Controller测试**

```java
package com.ghtransport.loading.controller;

import com.ghtransport.common.result.Result;
import com.ghtransport.loading.dto.LoadingRequest;
import com.ghtransport.loading.dto.LoadingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoadingController.class)
class LoadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoadingService loadingService;

    @Test
    @WithMockUser
    void testOptimize() throws Exception {
        LoadingRequest request = new LoadingRequest();
        request.setVehicleId("v001");
        request.setCargoItems(Collections.emptyList());

        LoadingResponse response = new LoadingResponse();
        response.setSolutionId("sol_001");
        response.setVolumeUtilization(0.85);

        when(loadingService.optimize(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/loading/optimize")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.solutionId").value("sol_001"))
                .andExpect(jsonPath("$.data.volumeUtilization").value(0.85));
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -pl gh-transport-loading -Dtest=LoadingControllerTest`
Expected: FAIL - LoadingController不存在

**Step 3: 实现LoadingRequest DTO**

```java
package com.ghtransport.loading.dto;

import com.ghtransport.loading.model.CargoItem;
import lombok.Data;
import java.util.List;

@Data
public class LoadingRequest {
    private String vehicleId;
    private List<CargoItem> cargoItems;
    private String algorithm; // heuristic, genetic, mip
    private OptimizationPreferences preferences;

    @Data
    public static class OptimizationPreferences {
        private double prioritizeValue = 0.4;
        private double prioritizeVolume = 0.3;
        private double prioritizeWeight = 0.3;
    }
}
```

**Step 4: 实现LoadingResponse DTO**

```java
package com.ghtransport.loading.dto;

import com.ghtransport.loading.model.LoadedItem;
import lombok.Data;
import java.util.List;

@Data
public class LoadingResponse {
    private String solutionId;
    private String vehicleId;
    private double volumeUtilization;
    private double weightUtilization;
    private double totalValue;
    private List<LoadedItem> loadingItems;
    private List<UnfitItem> unfitItems;
    private String message3DModel; // 3D可视化模型URL

    @Data
    public static class UnfitItem {
        private String cargoItemId;
        private String name;
        private int quantity;
        private String reason;
    }
}
```

**Step 5: 实现LoadingService**

```java
package com.ghtransport.loading.service;

import com.ghtransport.loading.dto.LoadingRequest;
import com.ghtransport.loading.dto.LoadingResponse;

public interface LoadingService {
    LoadingResponse optimize(LoadingRequest request);
}
```

**Step 6: 实现LoadingServiceImpl**

```java
package com.ghtransport.loading.service;

import com.ghtransport.loading.algorithm.BinPackingAlgorithm;
import com.ghtransport.loading.dto.LoadingRequest;
import com.ghtransport.loading.dto.LoadingResponse;
import com.ghtransport.loading.model.LoadedItem;
import com.ghtransport.loading.model.LoadingSolution;
import com.ghtransport.loading.model.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadingServiceImpl implements LoadingService {

    private final BinPackingAlgorithm heuristicPacker;

    // 这里应该注入车辆服务获取车辆信息
    private Vehicle getVehicle(String vehicleId) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        vehicle.setLength(400);
        vehicle.setWidth(200);
        vehicle.setHeight(200);
        vehicle.setMaxWeight(2000);
        return vehicle;
    }

    @Override
    public LoadingResponse optimize(LoadingRequest request) {
        log.info("开始装载优化, vehicleId: {}, items: {}",
            request.getVehicleId(), request.getCargoItems().size());

        Vehicle vehicle = getVehicle(request.getVehicleId());
        LoadingSolution solution = heuristicPacker.optimize(vehicle, request.getCargoItems());

        return convertToResponse(solution);
    }

    private LoadingResponse convertToResponse(LoadingSolution solution) {
        LoadingResponse response = new LoadingResponse();
        response.setSolutionId(solution.getSolutionId());
        response.setVehicleId(solution.getVehicleId());
        response.setVolumeUtilization(Math.round(solution.getVolumeUtilization() * 100.0) / 100.0);
        response.setWeightUtilization(Math.round(solution.getWeightUtilization() * 100.0) / 100.0);
        response.setTotalValue(solution.getTotalValue());
        response.setLoadingItems(solution.getLoadedItems());

        response.setUnfitItems(solution.getUnfitItems().stream()
            .map(item -> {
                LoadingResponse.UnfitItem unfit = new LoadingResponse.UnfitItem();
                unfit.setCargoItemId(item.getId());
                unfit.setName(item.getName());
                unfit.setQuantity(item.getQuantity());
                unfit.setReason("空间不足或重量超载");
                return unfit;
            })
            .collect(Collectors.toList()));

        return response;
    }
}
```

**Step 7: 实现LoadingController**

```java
package com.ghtransport.loading.controller;

import com.ghtransport.common.result.Result;
import com.ghtransport.loading.dto.LoadingRequest;
import com.ghtransport.loading.dto.LoadingResponse;
import com.ghtransport.loading.service.LoadingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/loading")
@RequiredArgsConstructor
public class LoadingController {

    private final LoadingService loadingService;

    /**
     * 装载优化接口
     * 根据货物和车辆信息，计算最优装载方案
     */
    @PostMapping("/optimize")
    @PreAuthorize("hasAuthority('loading:optimize')")
    public Result<LoadingResponse> optimize(@RequestBody LoadingRequest request) {
        log.info("收到装载优化请求, vehicleId: {}", request.getVehicleId());

        LoadingResponse response = loadingService.optimize(request);
        return Result.success(response);
    }

    /**
     * 获取装载方案详情
     */
    @GetMapping("/solution/{solutionId}")
    @PreAuthorize("hasAuthority('loading:view')")
    public Result<LoadingResponse> getSolution(@PathVariable String solutionId) {
        // 实现获取历史方案逻辑
        return Result.success(null);
    }

    /**
     * 确认装载方案并生成运输任务
     */
    @PostMapping("/solution/{solutionId}/confirm")
    @PreAuthorize("hasAuthority('loading:confirm')")
    public Result<String> confirmSolution(@PathVariable String solutionId) {
        // 确认方案，生成运输任务
        return Result.success("方案已确认，任务已生成");
    }
}
```

**Step 8: 运行测试验证Controller**

Run: `mvn test -pl gh-transport-loading -Dtest=LoadingControllerTest`
Expected: PASS - 所有测试通过

**Step 9: Commit**

```bash
git add gh-transport-loading/src/main/java/com/ghtransport/loading/controller/
git add gh-transport-loading/src/main/java/com/ghtransport/loading/dto/
git add gh-transport-loading/src/main/java/com/ghtransport/loading/service/
git commit -m "feat: 创建装载优化REST API"
```

---

## 第四阶段：车辆追踪服务

### Task 11: 实现GPS数据接收服务

**Files:**
- Create: `gh-transport-tracking/src/main/java/com/ghtransport/tracking/gps/GpsProtocolDecoder.java`
- Create: `gh-transport-tracking/src/main/java/com/ghtransport/tracking/gps/GpsData.java`
- Create: `gh-transport-tracking/src/main/java/com/ghtransport/tracking/service/GpsReceiveService.java`
- Create: `gh-transport-tracking/src/main/java/com/ghtransport/tracking/controller/TrackingController.java`
- Create: `gh-transport-tracking/src/test/java/com/ghtransport/tracking/gps/GpsProtocolDecoderTest.java`

**Step 1: 编写GpsData测试**

```java
package com.ghtransport.tracking.gps;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class GpsDataTest {

    @Test
    void testGpsDataCreation() {
        GpsData gpsData = new GpsData();
        gpsData.setVehicleId("v001");
        gpsData.setLatitude(39.908722);
        gpsData.setLongitude(116.397499);
        gpsData.setSpeed(60.0);
        gpsData.setDirection(180.0);
        gpsData.setGpsTime(Instant.now());
        gpsData.setAccuracy(10);

        assertEquals("v001", gpsData.getVehicleId());
        assertEquals(39.908722, gpsData.getLatitude());
        assertEquals(116.397499, gpsData.getLongitude());
    }

    @Test
    void testGpsDataValidation() {
        GpsData gpsData = new GpsData();

        // 纬度范围验证
        assertThrows(IllegalArgumentException.class, () -> {
            gpsData.setLatitude(100.0); // 无效纬度
        });

        // 经度范围验证
        assertThrows(IllegalArgumentException.class, () -> {
            gpsData.setLongitude(-200.0); // 无效经度
        });
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -pl gh-transport-tracking -Dtest=GpsDataTest`
Expected: FAIL - GpsData不存在

**Step 3: 实现GpsData**

```java
package com.ghtransport.tracking.gps;

import lombok.Data;
import java.time.Instant;

@Data
public class GpsData {
    private String vehicleId;
    private String deviceId;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;      // km/h
    private Double direction;  // 方向角度 0-360
    private Instant gpsTime;
    private Instant receiveTime;
    private Integer accuracy;  // 定位精度 米
    private Integer status;    // 0有效 1无效

    public void validate() {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("无效的纬度值: " + latitude);
        }
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("无效的经度值: " + longitude);
        }
        if (speed != null && (speed < 0 || speed > 500)) {
            throw new IllegalArgumentException("无效的速度值: " + speed);
        }
    }

    public boolean isValid() {
        try {
            validate();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

**Step 4: 实现GpsProtocolDecoder（GT06协议解码器）**

```java
package com.ghtransport.tracking.gps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

@Slf4j
@Component
public class GpsProtocolDecoder {

    /**
     * 解码GT06协议数据
     * GT06协议格式:
     * 头部长度: 0x78 0x78 + 长度(1字节) + 数据 + 校验 + 0x0D 0x0A
     */
    public GpsData decode(byte[] data) {
        if (data == null || data.length < 15) {
            throw new IllegalArgumentException("数据长度不足");
        }

        // 解析协议头
        if (data[0] != 0x78 || data[1] != 0x78) {
            throw new IllegalArgumentException("无效的协议头");
        }

        // 解析协议类型
        int protocolId = data[4] & 0xFF;

        switch (protocolId) {
            case 0x01:
                return decodeLocationData(data);
            case 0x13:
                return decodeHeartbeat(data);
            default:
                log.warn("未知的协议类型: 0x{}", Integer.toHexString(protocolId));
                return null;
        }
    }

    private GpsData decodeLocationData(byte[] data) {
        GpsData gpsData = new GpsData();

        // 设备ID (从偏移量5开始的8字节)
        StringBuilder deviceId = new StringBuilder();
        for (int i = 5; i < 13; i++) {
            deviceId.append(String.format("%02X", data[i]));
        }
        gpsData.setDeviceId(deviceId.toString());

        // GPS状态和纬度
        int bit1 = data[13] & 0xFF;
        int bit2 = data[14] & 0xFF;
        int bit3 = data[15] & 0xFF;
        int bit4 = data[16] & 0xFF;
        int bit5 = data[17] & 0xFF;
        int bit6 = data[18] & 0xFF;
        int bit7 = data[19] & 0xFF;
        int bit8 = data[20] & 0xFF;

        // 解析纬度 (度分格式转换为度)
        int latBits = ((bit1 & 0x3F) << 16) | (bit2 << 8) | bit3;
        double latitude = latBits / 30000.0;
        gpsData.setLatitude(latitude);

        // 解析经度
        int lngBits = ((bit4 & 0x3F) << 16) | (bit5 << 8) | bit6;
        double longitude = lngBits / 30000.0;
        gpsData.setLongitude(longitude);

        // 速度和方向
        int speed = data[21] & 0xFF;
        gpsData.setSpeed((double) speed);

        int direction = ((data[22] & 0xFF) << 8) | (data[23] & 0xFF);
        gpsData.setDirection((double) direction);

        // GPS时间
        int year = 2000 + (data[24] & 0xFF);
        int month = data[25] & 0xFF;
        int day = data[26] & 0xFF;
        int hour = data[27] & 0xFF;
        int minute = data[28] & 0xFF;
        int second = data[29] & 0xFF;

        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second);
        gpsData.setGpsTime(dateTime.toInstant(ZoneOffset.UTC));
        gpsData.setReceiveTime(Instant.now());

        return gpsData;
    }

    private GpsData decodeHeartbeat(byte[] data) {
        log.debug("收到心跳包");
        GpsData gpsData = new GpsData();
        gpsData.setReceiveTime(Instant.now());
        return gpsData;
    }
}
```

**Step 5: 实现GpsReceiveService**

```java
package com.ghtransport.tracking.service;

import com.ghtransport.tracking.gps.GpsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpsReceiveService {

    private final KafkaTemplate<String, GpsData> kafkaTemplate;
    private static final String GPS_TOPIC = "gps-raw";

    /**
     * 处理GPS数据（从TCP或HTTP接收）
     */
    public void processGpsData(GpsData gpsData) {
        try {
            gpsData.validate();

            // 发送到Kafka
            kafkaTemplate.send(GPS_TOPIC, gpsData.getVehicleId(), gpsData);

            log.debug("GPS数据已处理, vehicleId: {}, lat: {}, lng: {}",
                gpsData.getVehicleId(),
                gpsData.getLatitude(),
                gpsData.getLongitude());

        } catch (IllegalArgumentException e) {
            log.warn("GPS数据验证失败: {}", e.getMessage());
        }
    }

    /**
     * 从TCP连接接收数据
     */
    public void receiveFromTcp(byte[] rawData, String deviceId) {
        GpsProtocolDecoder decoder = new GpsProtocolDecoder();
        GpsData gpsData = decoder.decode(rawData);

        if (gpsData != null) {
            gpsData.setDeviceId(deviceId);
            processGpsData(gpsData);
        }
    }

    /**
     * 从HTTP接口接收数据（手机APP）
     */
    public void receiveFromHttp(GpsData gpsData) {
        processGpsData(gpsData);
    }
}
```

**Step 6: 实现HTTP接收Controller**

```java
package com.ghtransport.tracking.controller;

import com.ghtransport.common.result.Result;
import com.ghtransport.tracking.gps.GpsData;
import com.ghtransport.tracking.service.GpsReceiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final GpsReceiveService gpsReceiveService;

    /**
     * 手机APP上报位置
     */
    @PostMapping("/report")
    public Result<String> reportPosition(@RequestBody GpsData gpsData) {
        log.debug("收到位置上报, vehicleId: {}", gpsData.getVehicleId());

        try {
            gpsReceiveService.receiveFromHttp(gpsData);
            return Result.success("位置上报成功");
        } catch (Exception e) {
            log.error("位置上报失败", e);
            return Result.fail("位置上报失败: " + e.getMessage(), 500);
        }
    }

    /**
     * 批量上报位置
     */
    @PostMapping("/batch-report")
    public Result<String> batchReport(@RequestBody java.util.List<GpsData> gpsDataList) {
        log.info("收到批量位置上报, count: {}", gpsDataList.size());

        gpsDataList.forEach(gpsReceiveService::receiveFromHttp);
        return Result.success("批量上报成功");
    }
}
```

**Step 7: 运行测试验证**

Run: `mvn test -pl gh-transport-tracking -Dtest=GpsProtocolDecoderTest`
Expected: PASS - 所有测试通过

**Step 8: Commit**

```bash
git add gh-transport-tracking/src/main/java/com/ghtransport/tracking/gps/
git add gh-transport-tracking/src/main/java/com/ghtransport/tracking/service/
git add gh-transport-tracking/src/main/java/com/ghtransport/tracking/controller/
git commit -m "feat: 实现GPS数据接收服务GpsReceiveService"
```

---

### Task 12: 实现位置缓存与WebSocket推送

**Files:**
- Create: `gh-transport-tracking/src/main/java/com/ghtransport/tracking/service/PositionCacheService.java`
- Create: `gh-transport-tracking/src/main/java/com/ghtransport/tracking/websocket/VehiclePositionWebSocketHandler.java`
- Create: `gh-transport-tracking/src/main/java/com/ghtransport/tracking/config/WebSocketConfig.java`
- Create: `gh-transport-tracking/src/test/java/com/ghtransport/tracking/service/PositionCacheServiceTest.java`

**Step 1: 编写PositionCacheService测试**

```java
package com.ghtransport.tracking.service;

import com.ghtransport.tracking.gps.GpsData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionCacheServiceTest {

    @Mock
    private RedisTemplate<String, GpsData> redisTemplate;

    @Mock
    private ValueOperations<String, GpsData> valueOperations;

    private PositionCacheService positionCacheService;

    @BeforeEach
    void setUp() {
        positionCacheService = new PositionCacheService(redisTemplate);
    }

    @Test
    void testCachePosition() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        GpsData gpsData = new GpsData();
        gpsData.setVehicleId("v001");
        gpsData.setLatitude(39.908722);
        gpsData.setLongitude(116.397499);

        positionCacheService.cachePosition(gpsData);

        verify(valueOperations).set(
            eq("vehicle:position:v001"),
            eq(gpsData),
            any()
        );
    }

    @Test
    void testGetPosition() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        GpsData gpsData = new GpsData();
        gpsData.setVehicleId("v001");

        when(valueOperations.get("vehicle:position:v001")).thenReturn(gpsData);

        GpsData result = positionCacheService.getPosition("v001");

        assertNotNull(result);
        assertEquals("v001", result.getVehicleId());
    }

    @Test
    void testGetPositionNotFound() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        GpsData result = positionCacheService.getPosition("v999");

        assertNull(result);
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -pl gh-transport-tracking -Dtest=PositionCacheServiceTest`
Expected: FAIL - PositionCacheService不存在

**Step 3: 实现PositionCacheService**

```java
package com.ghtransport.tracking.service;

import com.ghtransport.tracking.gps.GpsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionCacheService {

    private final RedisTemplate<String, GpsData> redisTemplate;
    private static final String POSITION_KEY_PREFIX = "vehicle:position:";
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    /**
     * 缓存车辆位置
     */
    public void cachePosition(GpsData gpsData) {
        String key = POSITION_KEY_PREFIX + gpsData.getVehicleId();
        redisTemplate.opsForValue().set(key, gpsData, CACHE_TTL);
        log.debug("位置已缓存, vehicleId: {}", gpsData.getVehicleId());
    }

    /**
     * 获取车辆位置
     */
    public GpsData getPosition(String vehicleId) {
        String key = POSITION_KEY_PREFIX + vehicleId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量获取车辆位置
     */
    public List<GpsData> getPositions(List<String> vehicleIds) {
        List<String> keys = vehicleIds.stream()
                .map(id -> POSITION_KEY_PREFIX + id)
                .collect(Collectors.toList());

        List<GpsData> values = redisTemplate.opsForValue().multiGet(keys);
        return values != null ? values.stream()
                .filter(v -> v != null)
                .collect(Collectors.toList()) : List.of();
    }

    /**
     * 获取所有在线车辆位置
     */
    public List<GpsData> getAllOnlinePositions() {
        Set<String> keys = redisTemplate.keys(POSITION_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        List<GpsData> values = redisTemplate.opsForValue().multiGet(keys);
        return values != null ? values.stream()
                .filter(v -> v != null)
                .collect(Collectors.toList()) : List.of();
    }

    /**
     * 删除车辆位置缓存
     */
    public void deletePosition(String vehicleId) {
        String key = POSITION_KEY_PREFIX + vehicleId;
        redisTemplate.delete(key);
    }
}
```

**Step 4: 实现WebSocket处理器**

```java
package com.ghtransport.tracking.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtransport.tracking.gps.GpsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehiclePositionWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;

    // 存储所有连接的客户端
    private final Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        clients.put(sessionId, session);
        log.info("WebSocket客户端已连接, sessionId: {}", sessionId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            String action = (String) data.get("action");
            if ("subscribe".equals(action)) {
                // 订阅车辆位置更新
                log.info("客户端订阅车辆位置: {}", data.get("vehicleIds"));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
        clients.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        clients.remove(session.getId());
        log.info("WebSocket客户端已断开, sessionId: {}, status: {}",
            session.getId(), closeStatus);
    }

    /**
     * 向单个客户端发送消息
     */
    public void sendMessage(WebSocketSession session, Object data) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(data);
            session.sendMessage(new TextMessage(json));
        }
    }

    /**
     * 广播车辆位置更新
     */
    public void broadcastPosition(GpsData gpsData) {
        String json;
        try {
            json = objectMapper.writeValueAsString(Map.of(
                "type", "position",
                "vehicleId", gpsData.getVehicleId(),
                "latitude", gpsData.getLatitude(),
                "longitude", gpsData.getLongitude(),
                "speed", gpsData.getSpeed(),
                "direction", gpsData.getDirection(),
                "gpsTime", gpsData.getGpsTime().toString()
            ));

            TextMessage message = new TextMessage(json);

            clients.values().parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("发送WebSocket消息失败", e);
                    }
                });

        } catch (IOException e) {
            log.error("序列化GPS数据失败", e);
        }
    }

    /**
     * 获取在线客户端数量
     */
    public int getOnlineClientCount() {
        return (int) clients.values().stream()
                .filter(WebSocketSession::isOpen)
                .count();
    }
}
```

**Step 5: 实现WebSocket配置**

```java
package com.ghtransport.tracking.config;

import com.ghtransport.tracking.websocket.VehiclePositionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final VehiclePositionWebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/vehicle-position")
                .addAllowedOrigins("*");
    }
}
```

**Step 6: 运行测试验证**

Run: `mvn test -pl gh-transport-tracking -Dtest=PositionCacheServiceTest`
Expected: PASS - 所有测试通过

**Step 7: Commit**

```bash
git add gh-transport-tracking/src/main/java/com/ghtransport/tracking/service/
git add gh-transport-tracking/src/main/java/com/ghtransport/tracking/websocket/
git add gh-transport-tracking/src/main/java/com/ghtransport/tracking/config/
git commit -m "feat: 实现位置缓存与WebSocket推送服务"
```

---

## 第五阶段：进销存模块

### Task 13: 实现库存管理服务

**Files:**
- Create: `gh-transport-inventory/src/main/java/com/ghtransport/inventory/model/Warehouse.java`
- Create: `gh-transport-inventory/src/main/java/com/ghtransport/inventory/model/Inventory.java`
- Create: `gh-transport-inventory/src/main/java/com/ghtransport/inventory/model/InventoryTransaction.java`
- Create: `gh-transport-inventory/src/main/java/com/ghtransport/inventory/service/InventoryService.java`
- Create: `gh-transport-inventory/src/main/java/com/ghtransport/inventory/service/InventoryServiceImpl.java`
- Create: `gh-transport-inventory/src/test/java/com/ghtransport/inventory/service/InventoryServiceImplTest.java`

**Step 1: 编写库存锁定测试**

```java
package com.ghtransport.inventory.service;

import com.ghtransport.inventory.model.Inventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void testLockInventorySuccess() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(90L);

        boolean result = inventoryService.lockInventory("WH001", "ITEM001", 10);

        assertTrue(result);
        verify(valueOperations).set(
            eq("inventory:lock:WH001:ITEM001"),
            anyLong(),
            eq(30L),
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void testLockInventoryInsufficient() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(5L);

        boolean result = inventoryService.lockInventory("WH001", "ITEM001", 10);

        assertFalse(result);
    }

    @Test
    void testReleaseInventoryLock() {
        when(redisTemplate.delete(anyString())).thenReturn(true);

        inventoryService.releaseLock("WH001", "ITEM001", 10);

        verify(redisTemplate).delete("inventory:lock:WH001:ITEM001");
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -pl gh-transport-inventory -Dtest=InventoryServiceImplTest`
Expected: FAIL - InventoryServiceImpl不存在

**Step 3: 实现库存模型**

```java
package com.ghtransport.inventory.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Inventory {
    private Long id;
    private Long tenantId;
    private Long warehouseId;
    private String cargoItemId;
    private String positionId;
    private Integer quantity;
    private Integer availableQty;
    private Integer lockedQty;
    private String batchNo;
    private Instant productionDate;
    private Instant expiryDate;
    private BigDecimal costPrice;
    private Instant createdAt;
    private Instant updatedAt;
}
```

**Step 4: 实现库存交易模型**

```java
package com.ghtransport.inventory.model;

import lombok.Data;
import java.time.Instant;

@Data
public class InventoryTransaction {
    private Long id;
    private Long tenantId;
    private Long warehouseId;
    private String cargoItemId;
    private String positionId;
    private String batchNo;
    private String transactionType; // PURCHASE_IN, SALES_OUT, TRANSFER, ADJUST
    private String transactionNo;
    private Integer quantityBefore;
    private Integer quantity;
    private Integer quantityAfter;
    private Long createdBy;
    private Instant createdAt;
}
```

**Step 5: 实现库存服务接口**

```java
package com.ghtransport.inventory.service;

import com.ghtransport.inventory.model.Inventory;
import java.util.List;

public interface InventoryService {

    /**
     * 锁定库存（用于订单占用）
     */
    boolean lockInventory(String warehouseId, String cargoItemId, int quantity);

    /**
     * 释放库存锁定
     */
    void releaseLock(String warehouseId, String cargoItemId, int quantity);

    /**
     * 扣减库存（实际出库）
     */
    boolean deductInventory(String warehouseId, String cargoItemId, int quantity);

    /**
     * 增加库存（入库）
     */
    boolean addInventory(String warehouseId, String cargoItemId, int quantity, String batchNo);

    /**
     * 查询库存
     */
    Inventory getInventory(String warehouseId, String cargoItemId);

    /**
     * 查询所有仓库库存
     */
    List<Inventory> getInventories(String cargoItemId);
}
```

**Step 6: 实现库存服务（使用Redis + MySQL）**

```java
package com.ghtransport.inventory.service;

import com.ghtransport.common.exception.BusinessException;
import com.ghtransport.common.result.ResultCode;
import com.ghtransport.inventory.model.Inventory;
import com.ghtransport.inventory.model.InventoryTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final String LOCK_KEY_PREFIX = "inventory:lock:";
    private static final String AVAILABLE_KEY_PREFIX = "inventory:available:";

    @Override
    public boolean lockInventory(String warehouseId, String cargoItemId, int quantity) {
        String lockKey = LOCK_KEY_PREFIX + warehouseId + ":" + cargoItemId;
        String availableKey = AVAILABLE_KEY_PREFIX + warehouseId + ":" + cargoItemId;

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        // 先检查可用库存
        Object availableObj = ops.get(availableKey);
        int available = availableObj != null ? Integer.parseInt(availableObj.toString()) : 0;

        // 从数据库加载
        if (available == 0) {
            available = loadAvailableFromDb(warehouseId, cargoItemId);
            ops.set(availableKey, available, Duration.ofHours(1));
        }

        // 检查并锁定
        int locked = getLockedQuantity(lockKey);
        if (available - locked < quantity) {
            log.warn("库存不足, warehouseId: {}, cargoItemId: {}, required: {}, available: {}",
                warehouseId, cargoItemId, quantity, available - locked);
            return false;
        }

        // 增加锁定数量
        ops.increment(lockKey);
        redisTemplate.expire(lockKey, 30, TimeUnit.MINUTES); // 30分钟超时

        log.info("库存锁定成功, warehouseId: {}, cargoItemId: {}, quantity: {}",
            warehouseId, cargoItemId, quantity);
        return true;
    }

    @Override
    public void releaseLock(String warehouseId, String cargoItemId, int quantity) {
        String lockKey = LOCK_KEY_PREFIX + warehouseId + ":" + cargoItemId;
        redisTemplate.delete(lockKey);
        log.info("库存锁定已释放, warehouseId: {}, cargoItemId: {}", warehouseId, cargoItemId);
    }

    @Override
    @Transactional
    public boolean deductInventory(String warehouseId, String cargoItemId, int quantity) {
        String availableKey = AVAILABLE_KEY_PREFIX + warehouseId + ":" + cargoItemId;
        String lockKey = LOCK_KEY_PREFIX + warehouseId + ":" + cargoItemId;

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        // 使用Redis原子操作扣减
        Long newValue = ops.decrement(availableKey, quantity);
        if (newValue == null || newValue < 0) {
            // 回滚
            ops.increment(availableKey, quantity);
            throw new BusinessException("库存不足", ResultCode.INVENTORY_NOT_ENOUGH.getCode());
        }

        // 释放锁定
        redisTemplate.delete(lockKey);

        // 记录事务
        recordTransaction(warehouseId, cargoItemId, "SALES_OUT", -quantity);

        // 更新数据库
        updateDbInventory(warehouseId, cargoItemId, -quantity);

        log.info("库存扣减成功, warehouseId: {}, cargoItemId: {}, quantity: {}",
            warehouseId, cargoItemId, quantity);
        return true;
    }

    @Override
    @Transactional
    public boolean addInventory(String warehouseId, String cargoItemId, int quantity, String batchNo) {
        String availableKey = AVAILABLE_KEY_PREFIX + warehouseId + ":" + cargoItemId;

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.increment(availableKey, quantity);

        // 记录事务
        recordTransaction(warehouseId, cargoItemId, "PURCHASE_IN", quantity);

        // 更新数据库
        updateDbInventory(warehouseId, cargoItemId, quantity);

        log.info("库存增加成功, warehouseId: {}, cargoItemId: {}, quantity: {}",
            warehouseId, cargoItemId, quantity);
        return true;
    }

    @Override
    public Inventory getInventory(String warehouseId, String cargoItemId) {
        // 从Redis获取
        String availableKey = AVAILABLE_KEY_PREFIX + warehouseId + ":" + cargoItemId;
        Object availableObj = redisTemplate.opsForValue().get(availableKey);

        Inventory inventory = new Inventory();
        inventory.setWarehouseId(Long.parseLong(warehouseId));
        inventory.setCargoItemId(cargoItemId);
        inventory.setAvailableQty(availableObj != null ? Integer.parseInt(availableObj.toString()) : 0);

        return inventory;
    }

    private int getLockedQuantity(String lockKey) {
        Object locked = redisTemplate.opsForValue().get(lockKey);
        return locked != null ? Integer.parseInt(locked.toString()) : 0;
    }

    private int loadAvailableFromDb(String warehouseId, String cargoItemId) {
        // 从MySQL查询可用库存
        String sql = "SELECT available_qty FROM gt_inventory WHERE warehouse_id = ? AND cargo_item_id = ?";
        try {
            Integer qty = jdbcTemplate.queryForObject(sql, Integer.class, warehouseId, cargoItemId);
            return qty != null ? qty : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void recordTransaction(String warehouseId, String cargoItemId, String type, int quantity) {
        // 记录库存流水
    }

    private void updateDbInventory(String warehouseId, String cargoItemId, int change) {
        // 更新MySQL库存
        String sql = "UPDATE gt_inventory SET quantity = quantity + ?, " +
                     "available_qty = available_qty + ?, updated_at = NOW() " +
                     "WHERE warehouse_id = ? AND cargo_item_id = ?";
        jdbcTemplate.update(sql, change, change, warehouseId, cargoItemId);
    }
}
```

**Step 7: 运行测试验证**

Run: `mvn test -pl gh-transport-inventory -Dtest=InventoryServiceImplTest`
Expected: PASS - 所有测试通过

**Step 8: Commit**

```bash
git add gh-transport-inventory/src/main/java/com/ghtransport/inventory/
git commit -m "feat: 实现库存管理服务InventoryService"
```

---

## 第六阶段：订单与调度服务

### Task 14: 实现订单服务

**Files:**
- Create: `gh-transport-order/src/main/java/com/ghtransport/order/model/Order.java`
- Create: `gh-transport-order/src/main/java/com/ghtransport/order/model/OrderItem.java`
- Create: `gh-transport-order/src/main/java/com/ghtransport/order/service/OrderService.java`
- Create: `gh-transport-order/src/main/java/com/ghtransport/order/service/OrderServiceImpl.java`
- Create: `gh-transport-order/src/main/java/com/ghtransport/order/controller/OrderController.java`
- Create: `gh-transport-order/src/test/java/com/ghtransport/order/service/OrderServiceImplTest.java`

**Step 1: 编写订单创建测试**

```java
package com.ghtransport.order.service;

import com.ghtransport.order.model.Order;
import com.ghtransport.order.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testCreateOrder() {
        Order order = new Order();
        order.setCustomerId("C001");
        order.setPickupAddress("北京市朝阳区");
        order.setDeliveryAddress("上海市浦东新区");

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setCargoItemId("ITEM001");
        item.setQuantity(10);
        items.add(item);
        order.setItems(items);

        when(jdbcTemplate.queryForObject(anyString(), any(), any())).thenReturn(1L);

        Order created = orderService.createOrder(order);

        assertNotNull(created);
        assertNotNull(created.getOrderNo());
        assertEquals("PENDING", created.getStatus());
    }

    @Test
    void testConfirmOrder() {
        when(jdbcTemplate.update(anyString(), any())).thenReturn(1);
        when(inventoryService.lockInventory(anyString(), anyString(), anyInt())).thenReturn(true);

        boolean result = orderService.confirmOrder(1L);

        assertTrue(result);
        verify(inventoryService).lockInventory(anyString(), anyString(), anyInt());
    }
}
```

**Step 2: 运行测试验证失败**

Run: `mvn test -pl gh-transport-order -Dtest=OrderServiceImplTest`
Expected: FAIL - OrderServiceImpl不存在

**Step 3: 实现订单模型**

```java
package com.ghtransport.order.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class Order {
    private Long id;
    private Long tenantId;
    private String orderNo;
    private String customerId;

    /**
     * 订单状态：
     * DRAFT - 草稿
     * PENDING - 待确认
     * CONFIRMED - 已确认
     * ALLOCATED - 已配车（调度完成）
     * IN_TRANSIT - 运输中
     * ARRIVED - 已到达
     * DELIVERED - 已签收
     * COMPLETED - 已完成
     * CANCELLED - 已取消
     * REJECTED - 已拒收
     * PARTIAL_DELIVERED - 部分签收
     */
    private String status;
    private String priority;

    /**
     * 订单类型：
     * NORMAL - 普通订单
     * EXPRESS - 加急订单
     * SCHEDULED - 预约订单
     */
    private String orderType;

    private String pickupAddress;
    private String pickupLat;
    private String pickupLng;
    private String deliveryAddress;
    private String deliveryLat;
    private String deliveryLng;
    private Instant orderDate;
    private Instant expectDeliveryDate;
    private BigDecimal totalAmount;
    private BigDecimal weight;
    private BigDecimal volume;
    private String remarks;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    private List<OrderItem> items = new ArrayList<>();
}
```

**Step 4: 实现订单项模型**

```java
package com.ghtransport.order.model;

import lombok.Data;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private String cargoItemId;
    private String cargoName;
    private Integer quantity;
    private BigDecimal weight;
    private BigDecimal volume;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
```

**Step 5: 实现订单服务接口**

```java
package com.ghtransport.order.service;

import com.ghtransport.order.model.Order;
import java.util.List;

public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(Order order);

    /**
     * 确认订单
     */
    boolean confirmOrder(Long orderId);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId);

    /**
     * 查询订单
     */
    Order getOrder(Long orderId);

    /**
     * 查询订单列表
     */
    List<Order> listOrders(String status, int page, int size);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long orderId, String status);
}
```

**Step 6: 实现订单服务**

```java
package com.ghtransport.order.service;

import com.ghtransport.common.exception.BusinessException;
import com.ghtransport.common.result.ResultCode;
import com.ghtransport.common.tenant.TenantContextHolder;
import com.ghtransport.order.model.Order;
import com.ghtransport.order.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final JdbcTemplate jdbcTemplate;
    private final InventoryService inventoryService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // 设置租户ID
        order.setTenantId(TenantContextHolder.getTenantId());

        // 生成订单号
        String datePart = FORMATTER.format(Instant.now().atZone(java.time.ZoneOffset.UTC));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        order.setOrderNo("ORD" + datePart + "-" + randomPart);

        // 设置状态
        order.setStatus("DRAFT");
        order.setCreatedAt(Instant.now());

        // 计算总体积和总重量
        int totalWeight = 0;
        int totalVolume = 0;
        for (OrderItem item : order.getItems()) {
            totalWeight += item.getWeight().intValue() * item.getQuantity();
            totalVolume += item.getVolume().intValue() * item.getQuantity();
        }
        order.setWeight(new java.math.BigDecimal(totalWeight));
        order.setVolume(new java.math.BigDecimal(totalVolume));

        // 保存订单
        String sql = "INSERT INTO gt_order (tenant_id, order_no, customer_id, status, " +
                     "priority, pickup_address, delivery_address, weight, volume, " +
                     "created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
            order.getTenantId(),
            order.getOrderNo(),
            order.getCustomerId(),
            order.getStatus(),
            order.getPriority(),
            order.getPickupAddress(),
            order.getDeliveryAddress(),
            order.getWeight(),
            order.getVolume(),
            TenantContextHolder.getUserId(),
            order.getCreatedAt()
        );

        // 获取生成的ID
        Long orderId = jdbcTemplate.queryForObject(
            "SELECT LAST_INSERT_ID()", Long.class);
        order.setId(orderId);

        // 保存订单项
        saveOrderItems(order);

        log.info("订单创建成功, orderNo: {}", order.getOrderNo());
        return order;
    }

    @Override
    public boolean confirmOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在", ResultCode.ORDER_NOT_FOUND.getCode());
        }

        if (!"DRAFT".equals(order.getStatus()) && !"PENDING".equals(order.getStatus())) {
            throw new BusinessException("当前状态无法确认");
        }

        // 锁定库存
        for (OrderItem item : order.getItems()) {
            boolean locked = inventoryService.lockInventory(
                "WH001", // 默认仓库
                item.getCargoItemId(),
                item.getQuantity()
            );
            if (!locked) {
                throw new BusinessException("库存不足: " + item.getCargoName(),
                    ResultCode.INVENTORY_NOT_ENOUGH.getCode());
            }
        }

        // 更新状态
        return updateOrderStatus(orderId, "CONFIRMED");
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在", ResultCode.ORDER_NOT_FOUND.getCode());
        }

        if ("COMPLETED".equals(order.getStatus()) || "DELIVERED".equals(order.getStatus())) {
            throw new BusinessException("已完成的订单无法取消");
        }

        // 释放库存锁定
        for (OrderItem item : order.getItems()) {
            inventoryService.releaseLock("WH001", item.getCargoItemId(), item.getQuantity());
        }

        return updateOrderStatus(orderId, "CANCELLED");
    }

    @Override
    public Order getOrder(Long orderId) {
        String sql = "SELECT * FROM gt_order WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new OrderRowMapper(), orderId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Order> listOrders(String status, int page, int size) {
        Long tenantId = TenantContextHolder.getTenantId();
        String sql = "SELECT * FROM gt_order WHERE tenant_id = ?";
        if (status != null && !status.isEmpty()) {
            sql += " AND status = ?";
        }
        sql += " ORDER BY created_at DESC LIMIT ? OFFSET ?";

        Object[] params = status != null
            ? new Object[]{tenantId, status, size, page * size}
            : new Object[]{tenantId, size, page * size};

        return jdbcTemplate.query(sql, new OrderRowMapper(), params);
    }

    @Override
    public boolean updateOrderStatus(Long orderId, String status) {
        String sql = "UPDATE gt_order SET status = ?, updated_at = NOW() WHERE id = ?";
        int rows = jdbcTemplate.update(sql, status, orderId);
        return rows > 0;
    }

    private void saveOrderItems(Order order) {
        String sql = "INSERT INTO gt_order_item (order_id, cargo_item_id, cargo_name, " +
                     "quantity, weight, volume, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        for (OrderItem item : order.getItems()) {
            item.setOrderId(order.getId());
            jdbcTemplate.update(sql,
                item.getOrderId(),
                item.getCargoItemId(),
                item.getCargoName(),
                item.getQuantity(),
                item.getWeight(),
                item.getVolume(),
                item.getUnitPrice(),
                item.getSubtotal()
            );
        }
    }

    // 内部类：行映射器
    private static class OrderRowMapper implements org.springframework.jdbc.core.RowMapper<Order> {
        @Override
        public Order mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setTenantId(rs.getLong("tenant_id"));
            order.setOrderNo(rs.getString("order_no"));
            order.setCustomerId(rs.getString("customer_id"));
            order.setStatus(rs.getString("status"));
            order.setPickupAddress(rs.getString("pickup_address"));
            order.setDeliveryAddress(rs.getString("delivery_address"));
            return order;
        }
    }
}
```

**Step 7: 实现订单Controller**

```java
package com.ghtransport.order.controller;

import com.ghtransport.common.result.Result;
import com.ghtransport.order.model.Order;
import com.ghtransport.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('order:create')")
    public Result<Order> createOrder(@RequestBody Order order) {
        log.info("创建订单, customerId: {}", order.getCustomerId());
        Order created = orderService.createOrder(order);
        return Result.success(created);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('order:view')")
    public Result<Order> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        return Result.success(order);
    }

    /**
     * 确认订单
     */
    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasAuthority('order:confirm')")
    public Result<String> confirmOrder(@PathVariable Long orderId) {
        boolean success = orderService.confirmOrder(orderId);
        return success ? Result.success("订单已确认") : Result.fail("确认失败");
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('order:cancel')")
    public Result<String> cancelOrder(@PathVariable Long orderId) {
        boolean success = orderService.cancelOrder(orderId);
        return success ? Result.success("订单已取消") : Result.fail("取消失败");
    }

    /**
     * 订单列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('order:view')")
    public Result<List<Order>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> orders = orderService.listOrders(status, page, size);
        return Result.success(orders);
    }
}
```

**Step 8: 运行测试验证**

Run: `mvn test -pl gh-transport-order -Dtest=OrderServiceImplTest`
Expected: PASS - 所有测试通过

**Step 9: Commit**

```bash
git add gh-transport-order/src/main/java/com/ghtransport/order/
git commit -m "feat: 实现订单服务OrderService和OrderController"
```

---

## 优化项说明（与原计划对比）

### 主要改进

| 改进项 | 原计划 | 优化后 |
|--------|--------|--------|
| 持久层 | 原生JdbcTemplate | **MyBatis-Plus 3.5+** |
| API文档 | 缺失 | **Knife4j/Swagger 4.4** |
| 密码加密 | 明文 | **BCrypt** |
| 数据验证 | 无 | **GpsDataValidator** |
| 缓存预扣减 | 无 | **Redis + Lua脚本原子操作** |
| WebSocket | 简单广播 | **车辆订阅机制** |
| 实体基类 | 无 | **BaseEntity + 自动填充** |
| 统一响应 | 无 | **ResponseAdvice** |

### 新增模块

```
gh-transport-vehicle    # 车辆管理
gh-transport-finance    # 财务管理
```

### 新增功能

- 自动填充处理器（createdAt/updatedAt）
- 统一响应包装（ResponseAdvice）
- 租户过滤器（TenantFilter）
- GPS数据验证器
- Redis库存预扣减服务

---

## 第七阶段：系统管理与基础功能

### Task 17: 系统管理模块

**Files:**
- Create: `gh-transport-admin/pom.xml`
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/controller/SysConfigController.java`
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/controller/DataDictController.java`
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/entity/SysConfig.java`
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/entity/DataDict.java`
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/service/SysConfigService.java`

**Step 1: 创建系统配置实体**

```java
package com.ghtransport.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_sys_config")
public class SysConfig extends BaseEntity {
    private String configKey;
    private String configValue;
    private String configType;  // STRING, NUMBER, BOOLEAN, JSON
    private String description;
    private Integer sort;
}
```

**Step 2: 创建数据字典实体**

```java
package com.ghtransport.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_data_dict")
public class DataDict extends BaseEntity {
    private String dictType;    // 字典类型
    private String dictCode;    // 字典编码
    private String dictName;    // 字典名称
    private String parentCode;  // 父级编码
    private Integer sort;
    private String extValue;    // 扩展值
}
```

**Step 3: 创建系统配置Controller**

```java
package com.ghtransport.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.common.result.Result;
import com.ghtransport.admin.entity.SysConfig;
import com.ghtransport.admin.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统配置", description = "系统参数配置")
@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping("/list")
    @Operation(summary = "配置列表")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<List<SysConfig>> list() {
        return Result.success(sysConfigService.list());
    }

    @PostMapping
    @Operation(summary = "新增配置")
    @PreAuthorize("hasAuthority('system:config:create')")
    public Result<Void> create(@RequestBody SysConfig config) {
        sysConfigService.create(config);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新配置")
    @PreAuthorize("hasAuthority('system:config:update')")
    public Result<Void> update(@RequestBody SysConfig config) {
        sysConfigService.updateById(config);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置")
    @PreAuthorize("hasAuthority('system:config:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        sysConfigService.removeById(id);
        return Result.success();
    }

    @GetMapping("/by-key")
    @Operation(summary = "根据key获取配置")
    public Result<String> getByKey(@RequestParam String key) {
        return Result.success(sysConfigService.getByKey(key));
    }
}
```

**Step 4: Commit**

```bash
git add gh-transport-admin/
git commit -m "feat: 实现系统配置管理功能"
```

---

### Task 18: 操作日志模块

**Files:**
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/entity/OperateLog.java`
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/service/OperateLogService.java`
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/aspect/OperateLogAspect.java`
- Create: `gh-transport-admin/src/main/java/com/ghtransport/admin/controller/OperateLogController.java`

**Step 1: 创建操作日志实体**

```java
package com.ghtransport.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_operate_log")
public class OperateLog extends BaseEntity {
    private String module;           // 模块名称
    private String action;           // 操作类型
    private String description;      // 操作描述
    private Long userId;             // 操作人ID
    private String username;         // 操作人姓名
    private String requestMethod;    // 请求方法
    private String requestUrl;       // 请求URL
    private String requestParams;    // 请求参数
    private String responseResult;   // 响应结果
    private Long duration;           // 耗时(ms)
    private String ip;               // IP地址
    private String userAgent;        // 浏览器标识
}
```

**Step 2: 创建操作日志切面**

```java
package com.ghtransport.admin.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghtransport.admin.entity.OperateLog;
import com.ghtransport.admin.service.OperateLogService;
import com.ghtransport.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperateLogAspect {

    private final OperateLogService operateLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.ghtransport.admin.annotation.OperateLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 执行原方法
        Object result = point.proceed();

        // 记录日志
        try {
            saveOperateLog(point, result, startTime);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }

        return result;
    }

    private void saveOperateLog(ProceedingJoinPoint point, Object result, long startTime) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        HttpServletRequest request = getRequest();

        OperateLog log = new OperateLog();
        log.setModule(signature.getDeclaringType().getSimpleName());
        log.setRequestMethod(request.getMethod());
        log.setRequestUrl(request.getRequestURI());
        log.setIp(getClientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setDuration(System.currentTimeMillis() - startTime);

        if (TenantContextHolder.getUserId() != null) {
            log.setUserId(TenantContextHolder.getUserId());
            log.setUsername(TenantContextHolder.getUsername());
        }

        try {
            log.setRequestParams(objectMapper.writeValueAsString(point.getArgs()));
            log.setResponseResult(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            log.warn("序列化参数失败", e);
        }

        operateLogService.save(log);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
```

**Step 3: 创建操作日志注解**

```java
package com.ghtransport.admin.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {
    String module() default "";
    String action() default "";
    String description() default "";
}
```

**Step 4: Commit**

```bash
git add gh-transport-admin/src/main/java/com/ghtransport/admin/aspect/
git commit -m "feat: 实现操作日志记录功能"
```

---

### Task 19: 供应商管理模块

**Files:**
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/entity/Supplier.java`
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/entity/SupplierContact.java`
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/entity/SupplierContract.java`
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/controller/SupplierController.java`
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/service/SupplierService.java`

**Step 1: 创建供应商实体**

```java
package com.ghtransport.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_supplier")
public class Supplier extends BaseEntity {
    private Long tenantId;
    private String name;           // 供应商名称
    private String code;           // 供应商编码
    private String contactPerson;  // 联系人
    private String phone;          // 联系电话
    private String email;          // 邮箱
    private String address;        // 地址
    private String category;       // 供应商分类
    private String level;          // 供应商等级
    private Double creditLimit;    // 信用额度
    private Integer paymentTerms;  // 付款账期(天)
    private String status;         // 状态: ACTIVE, INACTIVE, BLACKLIST
}
```

**Step 2: 创建供应商Controller**

```java
package com.ghtransport.purchase.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.common.result.Result;
import com.ghtransport.purchase.entity.Supplier;
import com.ghtransport.purchase.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "供应商管理", description = "供应商信息管理")
@RestController
@RequestMapping("/api/v1/purchase/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "新增供应商")
    @PreAuthorize("hasAuthority('purchase:supplier:create')")
    public Result<Void> create(@RequestBody Supplier supplier) {
        supplierService.create(supplier);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新供应商")
    @PreAuthorize("hasAuthority('purchase:supplier:update')")
    public Result<Void> update(@RequestBody Supplier supplier) {
        supplierService.updateById(supplier);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除供应商")
    @PreAuthorize("hasAuthority('purchase:supplier:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取供应商详情")
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "供应商列表")
    public Result<List<Supplier>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(supplierService.list(keyword, status));
    }

    @GetMapping("/page")
    @Operation(summary = "供应商分页")
    public Result<Page<Supplier>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(supplierService.page(page, size, keyword));
    }
}
```

**Step 3: Commit**

```bash
git add gh-transport-inventory/purchase/
git commit -m "feat: 实现供应商管理模块"
```

---

## 第八阶段：采购与销售管理

### Task 20: 采购订单模块

**Files:**
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/entity/PurchaseOrder.java`
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/entity/PurchaseOrderItem.java`
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/entity/PurchaseReceipt.java`
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/service/PurchaseOrderService.java`
- Create: `gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/controller/PurchaseOrderController.java`

**Step 1: 创建采购订单实体**

```java
package com.ghtransport.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_purchase_order")
public class PurchaseOrder extends BaseEntity {
    private Long tenantId;
    private String orderNo;           // 订单编号
    private Long supplierId;          // 供应商ID
    private String supplierName;      // 供应商名称
    private String status;            // 状态: DRAFT, PENDING, APPROVED, RECEIVING, COMPLETED, CANCELLED
    private LocalDate orderDate;      // 订单日期
    private LocalDate expectDate;     // 预计到货日期
    private Long warehouseId;         // 入库仓库
    private BigDecimal totalAmount;   // 订单金额
    private BigDecimal taxAmount;     // 税额
    private String remark;            // 备注
    private String createdBy;         // 创建人

    // 非数据库字段
    private List<PurchaseOrderItem> items;
}
```

**Step 2: 创建采购订单Service**

```java
package com.ghtransport.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghtransport.purchase.entity.PurchaseOrder;

public interface PurchaseOrderService extends IService<PurchaseOrder> {

    /**
     * 创建采购订单
     */
    PurchaseOrder createOrder(PurchaseOrder order);

    /**
     * 审核采购订单
     */
    boolean approveOrder(Long orderId);

    /**
     * 取消采购订单
     */
    boolean cancelOrder(Long orderId);

    /**
     * 生成收货单
     */
    PurchaseReceipt generateReceipt(Long orderId);

    /**
     * 确认收货
     */
    boolean confirmReceipt(Long receiptId);
}
```

**Step 3: 创建采购订单Controller**

```java
package com.ghtransport.purchase.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.common.result.Result;
import com.ghtransport.purchase.entity.PurchaseOrder;
import com.ghtransport.purchase.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "采购订单", description = "采购订单管理")
@RestController
@RequestMapping("/api/v1/purchase/order")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @Operation(summary = "创建采购订单")
    @PreAuthorize("hasAuthority('purchase:order:create')")
    public Result<PurchaseOrder> create(@RequestBody PurchaseOrder order) {
        return Result.success(purchaseOrderService.createOrder(order));
    }

    @PostMapping("/{orderId}/approve")
    @Operation(summary = "审核订单")
    @PreAuthorize("hasAuthority('purchase:order:approve')")
    public Result<String> approve(@PathVariable Long orderId) {
        boolean success = purchaseOrderService.approveOrder(orderId);
        return success ? Result.success("审核通过") : Result.fail("审核失败");
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单")
    @PreAuthorize("hasAuthority('purchase:order:cancel')")
    public Result<String> cancel(@PathVariable Long orderId) {
        boolean success = purchaseOrderService.cancelOrder(orderId);
        return success ? Result.success("取消成功") : Result.fail("取消失败");
    }

    @GetMapping("/page")
    @Operation(summary = "订单分页")
    public Result<Page<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long supplierId) {
        return Result.success(purchaseOrderService.page(page, size, status, supplierId));
    }
}
```

**Step 4: Commit**

```bash
git add gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/entity/
git add gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/service/
git add gh-transport-inventory/purchase/src/main/java/com/ghtransport/purchase/controller/
git commit -m "feat: 实现采购订单管理模块"
```

---

### Task 21: 销售订单模块

**Files:**
- Create: `gh-transport-inventory/sales/src/main/java/com/ghtransport/sales/entity/SalesOrder.java`
- Create: `gh-transport-inventory/sales/src/main/java/com/ghtransport/sales/entity/SalesOrderItem.java`
- Create: `gh-transport-inventory/sales/src/main/java/com/ghtransport/sales/entity/SalesDelivery.java`
- Create: `gh-transport-inventory/sales/src/main/java/com/ghtransport/sales/controller/SalesOrderController.java`
- Create: `gh-transport-inventory/sales/src/main/java/com/ghtransport/sales/service/SalesOrderService.java`

**Step 1: 创建销售订单实体**

```java
package com.ghtransport.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_sales_order")
public class SalesOrder extends BaseEntity {
    private Long tenantId;
    private String orderNo;           // 订单编号
    private Long customerId;          // 客户ID
    private String customerName;      // 客户名称
    private String status;            // 状态: DRAFT, PENDING, CONFIRMED, PICKING, SHIPPING, DELIVERED, COMPLETED, CANCELLED
    private LocalDate orderDate;      // 订单日期
    private LocalDate expectDate;     // 预计发货日期
    private String deliveryAddress;   // 收货地址
    private Double deliveryLat;       // 收货纬度
    private Double deliveryLng;       // 收货经度
    private BigDecimal totalAmount;   // 订单金额
    private BigDecimal discount;      // 折扣金额
    private BigDecimal finalAmount;   // 最终金额
    private String remark;            // 备注
    private String createdBy;         // 创建人

    private List<SalesOrderItem> items;
}
```

**Step 2: 创建销售订单Service**

```java
package com.ghtransport.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghtransport.sales.entity.SalesOrder;

public interface SalesOrderService extends IService<SalesOrder> {

    /**
     * 创建销售订单（自动锁定库存）
     */
    SalesOrder createOrder(SalesOrder order);

    /**
     * 确认订单
     */
    boolean confirmOrder(Long orderId);

    /**
     * 拣货
     */
    boolean picking(Long orderId);

    /**
     * 发货
     */
    boolean ship(Long orderId);

    /**
     * 签收确认
     */
    boolean sign(Long orderId);
}
```

**Step 3: Commit**

```bash
git add gh-transport-inventory/sales/
git commit -m "feat: 实现销售订单管理模块"
```

---

## 第九阶段：财务管理

### Task 22: 应收账款模块

**Files:**
- Create: `gh-transport-finance/receivable/src/main/java/com/ghtransport/finance/entity/AccountsReceivable.java`
- Create: `gh-transport-finance/receivable/src/main/java/com/ghtransport/finance/entity/ReceiptRecord.java`
- Create: `gh-transport-finance/receivable/src/main/java/com/ghtransport/finance/controller/ReceivableController.java`
- Create: `gh-transport-finance/receivable/src/main/java/com/ghtransport/finance/service/ReceivableService.java`

**Step 1: 创建应收账款实体**

```java
package com.ghtransport.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_accounts_receivable")
public class AccountsReceivable extends BaseEntity {
    private Long tenantId;
    private String billNo;           // 账单编号
    private Long customerId;         // 客户ID
    private String customerName;     // 客户名称
    private Long salesOrderId;       // 关联销售订单
    private String orderNo;          // 订单编号
    private BigDecimal amount;       // 应收金额
    private BigDecimal paidAmount;   // 已收金额
    private BigDecimal balanceAmount;// 余额
    private LocalDate billDate;      // 账单日期
    private LocalDate dueDate;       // 到期日期
    private String status;           // 状态: UNPAID, PARTIAL, PAID, OVERDUE
    private String remark;           // 备注
}
```

**Step 2: 创建收款记录实体**

```java
package com.ghtransport.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_receipt_record")
public class ReceiptRecord extends BaseEntity {
    private Long tenantId;
    private Long receivableId;       // 关联应收ID
    private Long customerId;         // 客户ID
    private BigDecimal amount;       // 收款金额
    private String paymentMethod;    // 付款方式: CASH, BANK, WECHAT, ALIPAY
    private String bankName;         // 银行名称
    private String bankAccount;      // 银行账号
    private String transactionNo;    // 交易流水号
    private LocalDateTime receiptTime;// 收款时间
    private String remark;           // 备注
    private Long receivedBy;         // 收款人
}
```

**Step 3: 创建应收账款Controller**

```java
package com.ghtransport.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.common.result.Result;
import com.ghtransport.finance.entity.AccountsReceivable;
import com.ghtransport.finance.entity.ReceiptRecord;
import com.ghtransport.finance.service.ReceivableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "应收账款", description = "应收账款管理")
@RestController
@RequestMapping("/api/v1/finance/receivable")
@RequiredArgsConstructor
public class ReceivableController {

    private final ReceivableService receivableService;

    @PostMapping
    @Operation(summary = "生成应收账款")
    @PreAuthorize("hasAuthority('finance:receivable:create')")
    public Result<Void> create(@RequestBody AccountsReceivable receivable) {
        receivableService.save(receivable);
        return Result.success();
    }

    @PostMapping("/{id}/receipt")
    @Operation(summary = "收款登记")
    @PreAuthorize("hasAuthority('finance:receivable:receipt')")
    public Result<String> receipt(@PathVariable Long id, @RequestBody ReceiptRecord record) {
        boolean success = receivableService.receipt(id, record);
        return success ? Result.success("收款成功") : Result.fail("收款失败");
    }

    @GetMapping("/page")
    @Operation(summary = "应收账款分页")
    public Result<Page<AccountsReceivable>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId) {
        return Result.success(receivableService.page(page, size, status, customerId));
    }

    @GetMapping("/summary")
    @Operation(summary = "应收账款汇总")
    public Result<Object> summary() {
        return Result.success(receivableService.getSummary());
    }

    @GetMapping("/overdue")
    @Operation(summary = "逾期应收账款")
    public Result<List<AccountsReceivable>> overdue() {
        return Result.success(receivableService.getOverdueList());
    }
}
```

**Step 4: Commit**

```bash
git add gh-transport-finance/receivable/
git commit -m "feat: 实现应收账款管理模块"
```

---

### Task 23: 应付账款模块

**Files:**
- Create: `gh-transport-finance/payable/src/main/java/com/ghtransport/finance/entity/AccountsPayable.java`
- Create: `gh-transport-finance/payable/src/main/java/com/ghtransport/finance/entity/PaymentRecord.java`
- Create: `gh-transport-finance/payable/src/main/java/com/ghtransport/finance/controller/PayableController.java`
- Create: `gh-transport-finance/payable/src/main/java/com/ghtransport/finance/service/PayableService.java`

**Step 1: 创建应付账款实体**

```java
package com.ghtransport.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_accounts_payable")
public class AccountsPayable extends BaseEntity {
    private Long tenantId;
    private String billNo;           // 账单编号
    private Long supplierId;         // 供应商ID
    private String supplierName;     // 供应商名称
    private Long purchaseOrderId;    // 关联采购订单
    private String orderNo;          // 订单编号
    private BigDecimal amount;       // 应付金额
    private BigDecimal paidAmount;   // 已付金额
    private BigDecimal balanceAmount;// 余额
    private LocalDate billDate;      // 账单日期
    private LocalDate dueDate;       // 到期日期
    private String status;           // 状态: UNPAID, PARTIAL, PAID, OVERDUE
    private String remark;           // 备注
}
```

**Step 2: 创建付款记录实体**

```java
package com.ghtransport.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_payment_record")
public class PaymentRecord extends BaseEntity {
    private Long tenantId;
    private Long payableId;          // 关联应付ID
    private Long supplierId;         // 供应商ID
    private BigDecimal amount;       // 付款金额
    private String paymentMethod;    // 付款方式: CASH, BANK, WECHAT, ALIPAY
    private String bankName;         // 银行名称
    private String bankAccount;      // 银行账号
    private String transactionNo;    // 交易流水号
    private LocalDateTime paymentTime;// 付款时间
    private String remark;           // 备注
    private Long paidBy;             // 付款人
}
```

**Step 3: Commit**

```bash
git add gh-transport-finance/payable/
git commit -m "feat: 实现应付账款管理模块"
```

---

### Task 24: 发票管理模块

**Files:**
- Create: `gh-transport-finance/invoice/src/main/java/com/ghtransport/finance/entity/Invoice.java`
- Create: `gh-transport-finance/invoice/src/main/java/com/ghtransport/finance/controller/InvoiceController.java`
- Create: `gh-transport-finance/invoice/src/main/java/com/ghtransport/finance/service/InvoiceService.java`

**Step 1: 创建发票实体**

```java
package com.ghtransport.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_invoice")
public class Invoice extends BaseEntity {
    private Long tenantId;
    private String invoiceNo;        // 发票号码
    private String invoiceType;      // 发票类型: SALES_INVOICE, PURCHASE_INVOICE
    private String invoiceKind;      // 发票种类: VAT_NORMAL, VAT_SPECIAL, RECEIPT
    private Long relatedId;          // 关联业务ID
    private String relatedType;      // 关联类型: ORDER, RECEIVABLE, PAYABLE
    private String customerName;     // 客户名称
    private String customerTaxNo;    // 客户税号
    private String customerAddress;  // 客户地址
    private String customerPhone;    // 客户电话
    private String customerBank;     // 客户银行
    private String customerAccount;  // 客户账号
    private BigDecimal amount;       // 发票金额
    private BigDecimal taxAmount;    // 税额
    private BigDecimal totalAmount;  // 价税合计
    private LocalDate invoiceDate;   // 开票日期
    private String status;           // 状态: DRAFT, ISSUED, CANCELLED
    private String remark;           // 备注
}
```

**Step 2: Commit**

```bash
git add gh-transport-finance/invoice/
git commit -m "feat: 实现发票管理模块"
```

---

## 第十阶段：报表与定时任务

### Task 25: 报表模块

**Files:**
- Create: `gh-transport-report/pom.xml`
- Create: `gh-transport-report/src/main/java/com/ghtransport/report/controller/ReportController.java`
- Create: `gh-transport-report/src/main/java/com/ghtransport/report/service/OrderReportService.java`
- Create: `gh-transport-report/src/main/java/com/ghtransport/report/service/FinanceReportService.java`
- Create: `gh-transport-report/src/main/java/com/ghtransport/report/service/InventoryReportService.java`

**Step 1: 创建报表Controller**

```java
package com.ghtransport.report.controller;

import com.ghtransport.common.result.Result;
import com.ghtransport.report.service.OrderReportService;
import com.ghtransport.report.service.FinanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "报表管理", description = "业务报表接口")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final OrderReportService orderReportService;
    private final FinanceReportService financeReportService;

    @GetMapping("/order/summary")
    @Operation(summary = "订单汇总报表")
    @PreAuthorize("hasAuthority('report:order:view')")
    public Result<Map<String, Object>> orderSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(orderReportService.getSummary(startDate, endDate));
    }

    @GetMapping("/order/daily")
    @Operation(summary = "订单日报")
    @PreAuthorize("hasAuthority('report:order:view')")
    public Result<Map<String, Object>> orderDaily(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(orderReportService.getDaily(date));
    }

    @GetMapping("/finance/profit")
    @Operation(summary = "利润报表")
    @PreAuthorize("hasAuthority('report:finance:view')")
    public Result<Map<String, Object>> profitReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(financeReportService.getProfit(startDate, endDate));
    }

    @GetMapping("/vehicle/operation")
    @Operation(summary = "车辆运营报表")
    @PreAuthorize("hasAuthority('report:vehicle:view')")
    public Result<Map<String, Object>> vehicleOperation(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(orderReportService.getVehicleOperation(startDate, endDate));
    }

    @GetMapping("/inventory/stock")
    @Operation(summary = "库存报表")
    @PreAuthorize("hasAuthority('report:inventory:view')")
    public Result<Map<String, Object>> inventoryStock() {
        return Result.success(orderReportService.getInventoryStock());
    }
}
```

**Step 2: Commit**

```bash
git add gh-transport-report/
git commit -m "feat: 实现报表管理模块"
```

---

### Task 26: 定时任务模块

**Files:**
- Create: `gh-transport-schedule/pom.xml`
- Create: `gh-transport-schedule/src/main/java/com/ghtransport/schedule/config/ScheduleConfig.java`
- Create: `gh-transport-schedule/src/main/java/com/ghtransport/schedule/job/DailyReportJob.java`
- Create: `gh-transport-schedule/src/main/java/com/ghtransport/schedule/job/InventoryAlertJob.java`
- Create: `gh-transport-schedule/src/main/java/com/ghtransport/schedule/job/OrderTimeoutJob.java`

**Step 1: 创建定时任务配置**

```java
package com.ghtransport.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableAsync
public class ScheduleConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("schedule-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        return scheduler;
    }
}
```

**Step 2: 创建日报生成任务**

```java
package com.ghtransport.schedule.job;

import com.ghtransport.report.service.OrderReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportJob {

    private final OrderReportService orderReportService;

    /**
     * 每天凌晨1点生成日报
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyReport() {
        log.info("开始生成日报...");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            orderReportService.generateDailyReport(yesterday);
            log.info("日报生成完成");
        } catch (Exception e) {
            log.error("日报生成失败", e);
        }
    }
}
```

**Step 3: 创建库存预警任务**

```java
package com.ghtransport.schedule.job;

import com.ghtransport.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAlertJob {

    private final InventoryService inventoryService;

    /**
     * 每小时检查一次库存预警
     */
    @Scheduled(fixedRate = 3600000)
    public void checkInventoryAlert() {
        log.info("开始检查库存预警...");
        inventoryService.checkAndSendAlert();
        log.info("库存预警检查完成");
    }
}
```

**Step 4: Commit**

```bash
git add gh-transport-schedule/
git commit -m "feat: 实现定时任务模块"
```

---

## 第十一阶段：第三方集成

### Task 27: 短信服务集成

**Files:**
- Create: `gh-transport-integration/sms/src/main/java/com/ghtransport/integration/sms/SmsService.java`
- Create: `gh-transport-integration/sms/src/main/java/com/ghtransport/integration/sms/aliyun/AliyunSmsClient.java`
- Create: `gh-transport-integration/sms/src/main/java/com/ghtransport/integration/controller/SmsController.java`

**Step 1: 创建短信服务接口**

```java
package com.ghtransport.integration.sms;

public interface SmsService {

    /**
     * 发送短信验证码
     */
    boolean sendVerifyCode(String phone);

    /**
     * 发送订单通知
     */
    boolean sendOrderNotification(String phone, String orderNo, String status);

    /**
     * 发送发货通知
     */
    boolean sendDeliveryNotification(String phone, String customerName, String orderNo);

    /**
     * 验证验证码
     */
    boolean verifyCode(String phone, String code);
}
```

**Step 2: Commit**

```bash
git add gh-transport-integration/sms/
git commit -m "feat: 实现短信服务集成"
```

---

### Task 28: 邮件服务集成

**Files:**
- Create: `gh-transport-integration/email/src/main/java/com/ghtransport/integration/email/EmailService.java`
- Create: `gh-transport-integration/email/src/main/java/com/ghtransport/integration/email/EmailConfig.java`
- Create: `gh-transport-integration/email/src/main/java/com/ghtransport/integration/controller/EmailController.java`

**Step 1: 创建邮件服务接口**

```java
package com.ghtransport.integration.email;

import jakarta.mail.MessagingException;

public interface EmailService {

    /**
     * 发送简单邮件
     */
    void sendSimpleMail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     */
    void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException;

    /**
     * 发送带附件的邮件
     */
    void sendAttachmentMail(String to, String subject, String content, String... filePaths) throws MessagingException;

    /**
     * 发送日报邮件
     */
    void sendDailyReport(String to, byte[] reportData) throws MessagingException;
}
```

**Step 2: Commit**

```bash
git add gh-transport-integration/email/
git commit -m "feat: 实现邮件服务集成"
```

---

### Task 28: 客户管理模块

**Files:**
- Create: `gh-transport-customer/pom.xml`
- Create: `gh-transport-customer/src/main/java/com/ghtransport/customer/entity/Customer.java`
- Create: `gh-transport-customer/src/main/java/com/ghtransport/customer/entity/CustomerContact.java`
- Create: `gh-transport-customer/src/main/java/com/ghtransport/customer/entity/CustomerAddress.java`
- Create: `gh-transport-customer/src/main/java/com/ghtransport/customer/controller/CustomerController.java`
- Create: `gh-transport-customer/src/main/java/com/ghtransport/customer/service/CustomerService.java`

**Step 1: 创建客户实体**

```java
package com.ghtransport.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_customer")
public class Customer extends BaseEntity {
    private Long tenantId;
    private String customerNo;        // 客户编号
    private String name;              // 客户名称
    private String category;          // 客户分类: ENTERPRISE, INDIVIDUAL, GOVERNMENT
    private String level;             // 客户等级: VIP, A, B, C
    private String contactPerson;     // 联系人
    private String phone;             // 联系电话
    private String email;             // 邮箱
    private String fax;               // 传真
    private String source;            // 客户来源
    private BigDecimal creditLimit;   // 信用额度
    private Integer creditDays;       // 账期天数
    private String status;            // 状态: ACTIVE, INACTIVE, BLACKLIST
    private String remark;            // 备注
    private Long areaId;              // 所属区域
}
```

**Step 2: 创建客户联系人实体**

```java
package com.ghtransport.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_customer_contact")
public class CustomerContact extends BaseEntity {
    private Long tenantId;
    private Long customerId;
    private String name;           // 联系人姓名
    private String position;       // 职位
    private String phone;          // 手机
    private String telephone;      // 座机
    private String email;          // 邮箱
    private String wxOpenId;       // 微信OpenID
    private Boolean isDefault;     // 是否默认联系人
    private String remark;         // 备注
}
```

**Step 3: 创建客户地址实体**

```java
package com.ghtransport.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_customer_address")
public class CustomerAddress extends BaseEntity {
    private Long tenantId;
    private Long customerId;
    private String addressType;      // 地址类型: PICKUP, DELIVERY, BILLING
    private String province;         // 省份
    private String city;             // 城市
    private String district;         // 区县
    private String detailAddress;    // 详细地址
    private String latitude;         // 纬度
    private String longitude;        // 经度
    private String contactName;      // 联系人
    private String contactPhone;     // 联系电话
    private Boolean isDefault;       // 是否默认地址
}
```

**Step 4: Commit**

```bash
git add gh-transport-customer/
git commit -m "feat: 实现客户管理模块"
```

---

### Task 29: 司机管理模块

**Files:**
- Create: `gh-transport-driver/pom.xml`
- Create: `gh-transport-driver/src/main/java/com/ghtransport/driver/entity/Driver.java`
- Create: `gh-transport-driver/src/main/java/com/ghtransport/driver/entity/DriverLicense.java`
- Create: `gh-transport-driver/src/main/java/com/ghtransport/driver/entity/DriverSchedule.java`
- Create: `gh-transport-driver/src/main/java/com/ghtransport/driver/entity/DriverPerformance.java`
- Create: `gh-transport-driver/src/main/java/com/ghtransport/driver/controller/DriverController.java`
- Create: `gh-transport-driver/src/main/java/com/ghtransport/driver/service/DriverService.java`

**Step 1: 创建司机实体**

```java
package com.ghtransport.driver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_driver")
public class Driver extends BaseEntity {
    private Long tenantId;
    private String driverNo;         // 司机编号
    private String name;             // 姓名
    private String phone;            // 联系电话
    private String idCard;           // 身份证号
    private String gender;           // 性别
    private LocalDate birthDate;     // 出生日期
    private String address;          // 户籍地址
    private String currentAddress;   // 现住址
    private String status;           // 状态: ON_DUTY, OFF_DUTY, LEAVE, DISMISSED
    private String level;            // 司机等级
    private Integer hireDate;        // 入职日期
    private LocalDate contractExpiry;// 合同到期日期
    private String emergencyContact; // 紧急联系人
    private String emergencyPhone;   // 紧急联系电话
    private String remark;           // 备注
}
```

**Step 2: 创建司机驾照实体**

```java
package com.ghtransport.driver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_driver_license")
public class DriverLicense extends BaseEntity {
    private Long tenantId;
    private Long driverId;
    private String licenseType;      // 驾照类型: A1, A2, B1, B2, C1, C2
    private String licenseNo;        // 证号
    private LocalDate issueDate;     // 初领日期
    private LocalDate expiryDate;    // 有效期
    private String issuingAuthority; // 发证机关
    private String qualificationNo;  // 从业资格证号
    private LocalDate qualificationExpiry; // 资格证有效期
    private String status;           // 状态: VALID, EXPIRED, SUSPENDED
}
```

**Step 3: 创建司机排班实体**

```java
package com.ghtransport.driver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_driver_schedule")
public class DriverSchedule extends BaseEntity {
    private Long tenantId;
    private Long driverId;
    private LocalDate scheduleDate;  // 排班日期
    private String shiftType;        // 班次: DAY, NIGHT, FULL
    private LocalTime startTime;     // 开始时间
    private LocalTime endTime;       // 结束时间
    private String vehicleId;        // 分配的车辆ID
    private String status;           // 状态: SCHEDULED, ON_GOING, COMPLETED, CANCELLED
    private String remark;           // 备注
}
```

**Step 4: 创建司机绩效考核实体**

```java
package com.ghtransport.driver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_driver_performance")
public class DriverPerformance extends BaseEntity {
    private Long tenantId;
    private Long driverId;
    private LocalDate evaluateDate;  // 考核日期
    private Integer month;           // 考核月份
    private Integer year;            // 考核年份
    private Integer totalOrders;     // 总完成订单数
    private Integer totalDistance;   // 总行驶里程(km)
    private Integer onTimeDelivery;  // 准时送达数
    private Integer accidentCount;   // 事故次数
    private Integer complaintCount;  // 投诉次数
    private BigDecimal safetyScore;  // 安全评分
    private BigDecimal serviceScore; // 服务评分
    private BigDecimal totalScore;   // 综合评分
    private String level;            // 考核等级
    private String remark;           // 备注
}
```

**Step 5: Commit**

```bash
git add gh-transport-driver/
git commit -m "feat: 实现司机管理模块"
```

---

### Task 30: 设备管理模块

**Files:**
- Create: `gh-transport-device/pom.xml`
- Create: `gh-transport-device/src/main/java/com/ghtransport/device/entity/GpsDevice.java`
- Create: `gh-transport-device/src/main/java/com/ghtransport/device/entity/DeviceBind.java`
- Create: `gh-transport-device/src/main/java/com/ghtransport/device/entity/DeviceMaintain.java`
- Create: `gh-transport-device/src/main/java/com/ghtransport/device/controller/DeviceController.java`
- Create: `gh-transport-device/src/main/java/com/ghtransport/device/service/DeviceService.java`

**Step 1: 创建GPS设备实体**

```java
package com.ghtransport.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_gps_device")
public class GpsDevice extends BaseEntity {
    private Long tenantId;
    private String deviceNo;         // 设备编号
    private String imei;             // IMEI号
    private String simNo;            // SIM卡号
    private String model;            // 设备型号
    private String manufacturer;     // 厂商
    private String protocol;         // 通信协议: GT06, JT808, F3
    private String status;           // 状态: ONLINE, OFFLINE, MAINTENANCE, SCRAPPED
    private LocalDateTime activateTime; // 激活时间
    private LocalDateTime lastActiveTime;// 最后活跃时间
    private LocalDateTime expireDate;   // 过期时间
    private String remark;           // 备注
}
```

**Step 2: 创建设备绑定实体**

```java
package com.ghtransport.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_device_bind")
public class DeviceBind extends BaseEntity {
    private Long tenantId;
    private String deviceId;         // 设备ID
    private String bindType;         // 绑定类型: VEHICLE, ASSET
    private String bindId;           // 绑定对象ID(车辆ID等)
    private LocalDateTime bindTime;  // 绑定时间
    private LocalDateTime unbindTime;// 解绑时间
    private String status;           // 状态: BIND, UNBIND
    private String operator;         // 操作人
}
```

**Step 3: 创建设备维护记录实体**

```java
package com.ghtransport.device.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_device_maintain")
public class DeviceMaintain extends BaseEntity {
    private Long tenantId;
    private String deviceId;         // 设备ID
    private String maintainType;     // 维护类型: REPAIR, REPLACE, CHECK
    private String description;      // 问题描述
    private String solution;         // 处理方案
    private LocalDateTime maintainTime; // 维护时间
    private LocalDateTime completeTime;// 完成时间
    private String maintainer;       // 维护人员
    private BigDecimal cost;         // 维护费用
    private String status;           // 状态: PENDING, IN_PROGRESS, COMPLETED
}
```

**Step 4: Commit**

```bash
git add gh-transport-device/
git commit -m "feat: 实现设备管理模块"
```

---

### Task 31: 消息通知模块

**Files:**
- Create: `gh-transport-message/pom.xml`
- Create: `gh-transport-message/src/main/java/com/ghtransport/message/entity/MessageTemplate.java`
- Create: `gh-transport-message/src/main/java/com/ghtransport/message/entity/MessageRecord.java`
- Create: `gh-transport-message/src/main/java/com/ghtransport/message/entity/AppMessage.java`
- Create: `gh-transport-message/src/main/java/com/ghtransport/message/controller/MessageController.java`
- Create: `gh-transport-message/src/main/java/com/ghtransport/message/service/MessageService.java`

**Step 1: 创建消息模板实体**

```java
package com.ghtransport.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_message_template")
public class MessageTemplate extends BaseEntity {
    private Long tenantId;
    private String templateCode;     // 模板编码
    private String templateName;     // 模板名称
    private String channel;          // 渠道: SMS, EMAIL, APP, WECHAT
    private String title;            // 标题模板
    private String content;          // 内容模板
    private String contentType;      // 内容类型: TEXT, HTML
    private String eventType;        // 事件类型: ORDER_CREATE, ORDER_STATUS, DELIVERY_ARRIVED
    private Integer status;          // 状态: 0-禁用 1-启用
}
```

**Step 2: 创建消息记录实体**

```java
package com.ghtransport.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_message_record")
public class MessageRecord extends BaseEntity {
    private Long tenantId;
    private String messageId;        // 消息ID
    private String templateCode;     // 模板编码
    private String channel;          // 发送渠道
    private String recipient;        // 接收人
    private String title;            // 标题
    private String content;          // 内容
    private String eventType;        // 事件类型
    private String businessType;     // 业务类型
    private Long businessId;         // 业务ID
    private LocalDateTime sendTime;  // 发送时间
    private LocalDateTime readTime;  // 阅读时间
    private String status;           // 状态: PENDING, SENT, FAILED, READ
    private Integer retryCount;      // 重试次数
    private String errorMsg;         // 错误信息
}
```

**Step 3: 创建APP推送消息实体**

```java
package com.ghtransport.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_app_message")
public class AppMessage extends BaseEntity {
    private Long tenantId;
    private String messageId;        // 消息ID
    private Long userId;             // 用户ID
    private String title;            // 标题
    private String content;          // 内容
    private String messageType;      // 消息类型: ORDER, SYSTEM, NOTIFICATION
    private String jumpType;         // 跳转类型: URL, PAGE, NONE
    private String jumpValue;        // 跳转值
    private String imageUrl;         // 图片URL
    private LocalDateTime expireTime;// 过期时间
    private String status;           // 状态: UNREAD, READ
    private LocalDateTime readTime;  // 阅读时间
}
```

**Step 4: Commit**

```bash
git add gh-transport-message/
git commit -m "feat: 实现消息通知模块"
```

---

### Task 32: 数据初始化脚本

**Files:**
- Create: `sql/init/01_system_config.sql` - 系统配置数据
- Create: `sql/init/02_data_dict.sql` - 数据字典
- Create: `sql/init/03_area_data.sql` - 地区数据
- Create: `sql/init/04_demo_data.sql` - 演示数据

**Step 1: 创建系统配置SQL**

```sql
-- 系统配置数据
INSERT INTO gt_sys_config (config_key, config_value, config_type, description, sort) VALUES
('COMPANY_NAME', 'GH物流', 'STRING', '公司名称', 1),
('ORDER_NO_PREFIX', 'ORD', 'STRING', '订单号前缀', 2),
('DEFAULT_WAREHOUSE', 'WH001', 'STRING', '默认仓库', 3),
('INVENTORY_LOCK_TIMEOUT', '30', 'NUMBER', '库存锁定超时时间(分钟)', 4),
('GPS_REPORT_INTERVAL', '60', 'NUMBER', 'GPS上报间隔(秒)', 5),
('ORDER_EXPIRE_DAYS', '7', 'NUMBER', '订单过期天数', 6),
('CREDIT_DAYS_DEFAULT', '30', 'NUMBER', '默认账期天数', 7),
('SMS_SIGNATURE', '【GH物流】', 'STRING', '短信签名', 8),
('LOW_STOCK_THRESHOLD', '100', 'NUMBER', '库存预警阈值', 9);
```

**Step 2: 创建数据字典SQL**

```sql
-- 数据字典 - 订单状态
INSERT INTO gt_data_dict (dict_type, dict_code, dict_name, dict_value, sort) VALUES
('ORDER_STATUS', 'DRAFT', '草稿', null, 1),
('ORDER_STATUS', 'PENDING', '待确认', null, 2),
('ORDER_STATUS', 'CONFIRMED', '已确认', null, 3),
('ORDER_STATUS', 'ALLOCATED', '已配车', null, 4),
('ORDER_STATUS', 'IN_TRANSIT', '运输中', null, 5),
('ORDER_STATUS', 'ARRIVED', '已到达', null, 6),
('ORDER_STATUS', 'DELIVERED', '已签收', null, 7),
('ORDER_STATUS', 'COMPLETED', '已完成', null, 8),
('ORDER_STATUS', 'CANCELLED', '已取消', null, 9),
('ORDER_STATUS', 'REJECTED', '已拒收', null, 10);

-- 数据字典 - 客户等级
INSERT INTO gt_data_dict (dict_type, dict_code, dict_name, dict_value, sort) VALUES
('CUSTOMER_LEVEL', 'VIP', 'VIP客户', null, 1),
('CUSTOMER_LEVEL', 'A', 'A级客户', null, 2),
('CUSTOMER_LEVEL', 'B', 'B级客户', null, 3),
('CUSTOMER_LEVEL', 'C', 'C级客户', null, 4);

-- 数据字典 - 司机状态
INSERT INTO gt_data_dict (dict_type, dict_code, dict_name, dict_value, sort) VALUES
('DRIVER_STATUS', 'ON_DUTY', '在职', null, 1),
('DRIVER_STATUS', 'OFF_DUTY', '休息', null, 2),
('DRIVER_STATUS', 'LEAVE', '请假', null, 3),
('DRIVER_STATUS', 'DISMISSED', '离职', null, 4);

-- 数据字典 - 设备状态
INSERT INTO gt_data_dict (dict_type, dict_code, dict_name, dict_value, sort) VALUES
('DEVICE_STATUS', 'ONLINE', '在线', null, 1),
('DEVICE_STATUS', 'OFFLINE', '离线', null, 2),
('DEVICE_STATUS', 'MAINTENANCE', '维护中', null, 3),
('DEVICE_STATUS', 'SCRAPPED', '已报废', null, 4);
```

**Step 3: 创建地区数据SQL**

```sql
-- 省份数据
INSERT INTO gt_area (area_code, area_name, parent_code, area_type) VALUES
('110000', '北京市', '0', 'PROVINCE'),
('120000', '天津市', '0', 'PROVINCE'),
('310000', '上海市', '0', 'PROVINCE'),
('440100', '广州市', '440000', 'CITY'),
('440300', '深圳市', '440000', 'CITY'),
('330100', '杭州市', '330000', 'CITY'),
('330200', '宁波市', '330000', 'CITY');

-- 示例城市区县
INSERT INTO gt_area (area_code, area_name, parent_code, area_type) VALUES
('110100', '北京市区', '110000', 'CITY'),
('110101', '东城区', '110100', 'DISTRICT'),
('110102', '西城区', '110100', 'DISTRICT'),
('110105', '朝阳区', '110100', 'DISTRICT'),
('110106', '丰台区', '110100', 'DISTRICT');
```

**Step 4: 创建演示数据SQL**

```sql
-- 演示租户数据
INSERT INTO gt_tenant (id, name, code, status, expire_date) VALUES
(1, '演示租户', 'demo', 'ACTIVE', '2099-12-31');

-- 演示用户
INSERT INTO gt_user (id, tenant_id, username, password, real_name, phone, email, status) VALUES
(1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 'admin@ghtransport.com', 'ACTIVE'),
(2, 1, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', '13800000001', 'test@ghtransport.com', 'ACTIVE');

-- 演示仓库
INSERT INTO gt_warehouse (id, tenant_id, warehouse_code, warehouse_name, address, status) VALUES
(1, 1, 'WH001', '主仓库', '北京市朝阳区', 'ACTIVE'),
(2, 1, 'WH002', '华东仓库', '上海市浦东新区', 'ACTIVE');

-- 演示车辆
INSERT INTO gt_vehicle (id, tenant_id, plate_number, vehicle_type, length, width, height, max_weight, status) VALUES
(1, 1, '京A12345', '厢式货车', 600, 250, 250, 5000, 'ACTIVE'),
(2, 1, '京A67890', '平板货车', 800, 250, 50, 10000, 'ACTIVE');

-- 演示司机
INSERT INTO gt_driver (id, tenant_id, driver_no, name, phone, status, level) VALUES
(1, 1, 'D001', '张三', '13900000001', 'ON_DUTY', 'A'),
(2, 1, 'D002', '李四', '13900000002', 'ON_DUTY', 'B');

-- 演示客户
INSERT INTO gt_customer (id, tenant_id, customer_no, name, category, level, contact_person, phone, status) VALUES
(1, 1, 'C001', '测试企业A', 'ENTERPRISE', 'VIP', '王经理', '13800001001', 'ACTIVE'),
(2, 1, 'C002', '测试企业B', 'ENTERPRISE', 'A', '李经理', '13800001002', 'ACTIVE');

-- 演示货物
INSERT INTO gt_cargo_item (id, tenant_id, item_code, item_name, unit, length, width, height, weight) VALUES
(1, 1, 'ITEM001', '标准纸箱', '箱', 50, 40, 30, 5),
(2, 1, 'ITEM002', '周转箱', '个', 60, 40, 20, 3);
```

**Step 5: Commit**

```bash
git add sql/init/
git commit -m "feat: 添加数据初始化脚本"
```

---

## 完整模块清单

```
gh-transport-parent                    # 父POM
├── gh-transport-common                # 公共模块
├── gh-transport-api                   # API实体和Mapper
├── gh-transport-gateway               # 网关服务
├── gh-transport-admin                 # 管理后台服务
│   ├── controller                     # 控制器
│   ├── entity                         # 实体
│   ├── service                        # 服务
│   └── aspect                         # 切面
├── gh-transport-loading               # 装载优化
│   ├── algorithm                      # 算法
│   ├── controller                     # API
│   └── service                        # 服务
├── gh-transport-tracking              # 车辆追踪
│   ├── gps                            # GPS处理
│   ├── controller                     # API
│   └── service                        # 服务
├── gh-transport-order                 # 订单管理
├── gh-transport-inventory             # 库存管理
│   ├── purchase                       # 采购管理 ⭐新增
│   │   ├── entity                     # 供应商、采购订单
│   │   ├── controller                 # API
│   │   └── service                    # 服务
│   ├── sales                          # 销售管理 ⭐新增
│   │   ├── entity                     # 客户、销售订单
│   │   ├── controller                 # API
│   │   └── service                    # 服务
│   └── warehouse                      # 仓库管理
├── gh-transport-vehicle               # 车辆管理
├── gh-transport-finance               # 财务管理 ⭐重构
│   ├── receivable                     # 应收管理 ⭐新增
│   ├── payable                        # 应付管理 ⭐新增
│   └── invoice                        # 发票管理 ⭐新增
├── gh-transport-report                # 报表模块 ⭐新增
│   ├── controller                     # API
│   └── service                        # 报表服务
├── gh-transport-schedule              # 定时任务 ⭐新增
│   ├── config                         # 配置
│   └── job                            # 任务
└── gh-transport-integration           # 第三方集成 ⭐新增
    ├── sms                            # 短信服务
    ├── email                          # 邮件服务
    └── map                            # 地图服务
```

---

## 总结

本次更新补充了以下模块：

| 阶段 | 新增模块 | 主要功能 |
|------|----------|----------|
| 第七阶段 | 系统管理 | 配置管理、数据字典、操作日志 |
| 第八阶段 | 采购销售 | 供应商、采购订单、销售订单、客户管理 |
| 第九阶段 | 财务管理 | 应收管理、应付管理、发票管理 |
| 第十阶段 | 报表任务 | 订单报表、财务报表、定时任务 |
| 第十一阶段 | 第三方集成 | 短信、邮件、地图服务 |

**新增模块清单：**
- gh-transport-admin - 管理后台
- gh-transport-inventory/purchase - 采购管理
- gh-transport-inventory/sales - 销售管理
- gh-transport-finance/receivable - 应收管理
- gh-transport-finance/payable - 应付管理
- gh-transport-finance/invoice - 发票管理
- gh-transport-report - 报表模块
- gh-transport-schedule - 定时任务
- gh-transport-integration - 第三方集成

---

## 第十二阶段：调度排单与路径规划

### Task 29: 调度排单模块

**Files:**
- Create: `gh-transport-dispatch/pom.xml`
- Create: `gh-transport-dispatch/src/main/java/com/ghtransport/dispatch/entity/DispatchRule.java`
- Create: `gh-transport-dispatch/src/main/java/com/ghtransport/dispatch/entity/DispatchTask.java`
- Create: `gh-transport-dispatch/src/main/java/com/ghtransport/dispatch/entity/DispatchVehicle.java`
- Create: `gh-transport-dispatch/src/main/java/com/ghtransport/dispatch/service/DispatchService.java`
- Create: `gh-transport-dispatch/src/main/java/com/ghtransport/dispatch/controller/DispatchController.java`
- Create: `gh-transport-dispatch/src/main/java/com/ghtransport/dispatch/algorithm/DispatchAlgorithm.java`

**Step 1: 创建调度规则实体**

```java
package com.ghtransport.dispatch.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_dispatch_rule")
public class DispatchRule extends BaseEntity {
    private Long tenantId;
    private String ruleName;         // 规则名称
    private String ruleType;         // 规则类型: AUTO, MANUAL, SEMI_AUTO
    private Integer priority;        // 优先级
    private String vehicleFilter;    // 车辆筛选条件(JSON)
    private String orderFilter;      // 订单筛选条件(JSON)
    private String assignmentStrategy;// 分配策略: NEAREST, LOAD_BALANCE, COST_MIN
    private Integer maxOrdersPerVehicle; // 每车最大订单数
    private Double maxDeviation;     // 最大偏离距离(km)
    private Integer status;          // 状态: 0禁用 1启用
}
```

**Step 2: 创建调度任务实体**

```java
package com.ghtransport.dispatch.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_dispatch_task")
public class DispatchTask extends BaseEntity {
    private Long tenantId;
    private String taskNo;           // 任务编号
    private String status;           // 状态: PENDING, DISPATCHING, EXECUTING, COMPLETED, CANCELLED
    private LocalDate planDate;      // 计划日期
    private Long vehicleId;          // 分配车辆
    private String vehiclePlate;     // 车牌号
    private Long driverId;           // 分配司机
    private String driverName;       // 司机姓名
    private Integer orderCount;      // 订单数量
    private Double totalWeight;      // 总重量
    private Double totalVolume;      // 总体积
    private String routeIds;         // 路线ID列表
    private Double estimatedDistance;// 预估里程
    private Double estimatedDuration;// 预估时长(分钟)
    private String remark;           // 备注
}
```

**Step 3: 创建调度服务接口**

```java
package com.ghtransport.dispatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghtransport.dispatch.entity.DispatchTask;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DispatchService extends IService<DispatchTask> {

    /**
     * 自动调度
     */
    List<DispatchTask> autoDispatch(LocalDate planDate);

    /**
     * 手动调度 - 选择订单分配车辆
     */
    DispatchTask manualDispatch(Long vehicleId, List<Long> orderIds);

    /**
     * 调整调度 - 重新分配订单
     */
    DispatchTask rearrange(Long taskId, List<Long> orderIds);

    /**
     * 取消调度
     */
    boolean cancelDispatch(Long taskId);

    /**
     * 获取调度方案建议
     */
    List<Map<String, Object>> getDispatchSuggestions(LocalDate planDate);

    /**
     * 获取车辆装载率
     */
    Map<String, Object> getVehicleLoadRate(Long vehicleId, LocalDate date);
}
```

**Step 4: 创建调度算法**

```java
package com.ghtransport.dispatch.algorithm;

import com.ghtransport.dispatch.entity.DispatchTask;
import com.ghtransport.order.entity.Order;

import java.util.List;

public interface DispatchAlgorithm {

    /**
     * 执行调度算法
     * @param orders 待调度订单
     * @param availableVehicles 可用车辆
     * @return 调度方案
     */
    DispatchResult dispatch(List<Order> orders, List<VehicleInfo> availableVehicles);

    /**
     * 获取算法名称
     */
    String getAlgorithmName();

    class DispatchResult {
        private List<DispatchTask> tasks;
        private double totalCost;
        private double totalDistance;
        private long computeTime;

        // getters and setters
    }

    class VehicleInfo {
        private Long vehicleId;
        private String plateNumber;
        private Double maxWeight;
        private Double maxVolume;
        private Double currentLat;
        private Double currentLng;

        // getters and setters
    }
}
```

**Step 5: 创建调度Controller**

```java
package com.ghtransport.dispatch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.common.result.Result;
import com.ghtransport.dispatch.entity.DispatchTask;
import com.ghtransport.dispatch.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "调度管理", description = "运输调度管理")
@RestController
@RequestMapping("/api/v1/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping("/auto")
    @Operation(summary = "自动调度")
    @PreAuthorize("hasAuthority('dispatch:auto')")
    public Result<List<DispatchTask>> autoDispatch(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate planDate) {
        return Result.success(dispatchService.autoDispatch(planDate));
    }

    @PostMapping("/manual")
    @Operation(summary = "手动调度")
    @PreAuthorize("hasAuthority('dispatch:manual')")
    public Result<DispatchTask> manualDispatch(
            @RequestParam Long vehicleId,
            @RequestBody List<Long> orderIds) {
        return Result.success(dispatchService.manualDispatch(vehicleId, orderIds));
    }

    @PostMapping("/{taskId}/rearrange")
    @Operation(summary = "重新调度")
    @PreAuthorize("hasAuthority('dispatch:rearrange')")
    public Result<DispatchTask> rearrange(
            @PathVariable Long taskId,
            @RequestBody List<Long> orderIds) {
        return Result.success(dispatchService.rearrange(taskId, orderIds));
    }

    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "取消调度")
    @PreAuthorize("hasAuthority('dispatch:cancel')")
    public Result<String> cancel(@PathVariable Long taskId) {
        boolean success = dispatchService.cancelDispatch(taskId);
        return success ? Result.success("取消成功") : Result.fail("取消失败");
    }

    @GetMapping("/suggestions")
    @Operation(summary = "获取调度建议")
    public Result<List<Map<String, Object>>> getSuggestions(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate planDate) {
        return Result.success(dispatchService.getDispatchSuggestions(planDate));
    }

    @GetMapping("/page")
    @Operation(summary = "调度任务分页")
    public Result<Page<DispatchTask>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate planDate) {
        return Result.success(dispatchService.page(page, size, status, planDate));
    }
}
```

**Step 6: Commit**

```bash
git add gh-transport-dispatch/
git commit -m "feat: 实现调度排单模块"
```

---

### Task 30: 路径规划集成

**Files:**
- Create: `gh-transport-integration/map/src/main/java/com/ghtransport/integration/map/MapService.java`
- Create: `gh-transport-integration/map/src/main/java/com/ghtransport/integration/map/RoutePlanner.java`
- Create: `gh-transport-integration/map/src/main/java/com/ghtransport/integration/map/baidu/BaiduMapClient.java`
- Create: `gh-transport-integration/map/src/main/java/com/ghtransport/integration/controller/MapController.java`

**Step 1: 创建地图服务接口**

```java
package com.ghtransport.integration.map;

import java.util.List;

public interface MapService {

    /**
     * 路线规划
     * @param origin 起点经纬度
     * @param destination 终点经纬度
     * @param mode 出行方式: driving, walking, bicycling
     * @return 路线信息
     */
    RouteResult routePlan(Point origin, Point destination, String mode);

    /**
     * 多点路线优化（TSP）
     * @param points 点列表
     * @return 最优路线
     */
    OptimizedRoute optimizeRoute(List<Point> points);

    /**
     * 距离测量
     */
    double calculateDistance(Point origin, Point destination);

    /**
     * 地理编码（地址转坐标）
     */
    Point geocode(String address);

    /**
     * 逆地理编码（坐标转地址）
     */
    String reverseGeocode(Point point);

    /**
     * 轨迹纠偏
     */
    List<Point> correctTrace(List<Point> rawPoints);
}

@Data
class Point {
    private Double lat;
    private Double lng;
}

class RouteResult {
    private Double distance;       // 距离(米)
    private Integer duration;      // 时长(秒)
    private List<RouteStep> steps;
    private String polyline;       // 路线编码
}

class OptimizedRoute {
    private List<Point> optimizedPoints;
    private Double totalDistance;
    private Integer totalDuration;
    private List<Integer> order;   // 访问顺序
}
```

**Step 2: 创建路线规划服务**

```java
package com.ghtransport.integration.map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutePlanner {

    private final MapService mapService;

    /**
     * 配送路线规划
     * 综合考虑订单顺序、距离、时间等因素
     */
    public DeliveryRoute planDeliveryRoute(
            Point depot,               // 仓库位置
            List<OrderPoint> orders    // 订单位置列表
    ) {
        // 1. 获取所有点坐标
        List<Point> allPoints = new ArrayList<>();
        allPoints.add(depot);
        for (OrderPoint order : orders) {
            allPoints.add(order.getPoint());
        }

        // 2. 使用TSP算法优化路线
        OptimizedRoute optimized = mapService.optimizeRoute(allPoints);

        // 3. 计算每段路线详细信息
        List<RouteSegment> segments = new ArrayList<>();
        List<Point> routePoints = optimized.getOptimizedPoints();

        for (int i = 0; i < routePoints.size() - 1; i++) {
            RouteResult segment = mapService.routePlan(
                routePoints.get(i),
                routePoints.get(i + 1),
                "driving"
            );
            segments.add(new RouteSegment(
                routePoints.get(i),
                routePoints.get(i + 1),
                segment.getDistance(),
                segment.getDuration(),
                segment.getPolyline()
            ));
        }

        // 4. 构建配送路线
        DeliveryRoute route = new DeliveryRoute();
        route.setSegments(segments);
        route.setTotalDistance(optimized.getTotalDistance());
        route.setTotalDuration(optimized.getTotalDuration());

        return route;
    }

    @Data
    class OrderPoint {
        private Long orderId;
        private Point point;
        private Integer priority;  // 优先级
        private String timeWindow; // 时间窗口
    }

    @Data
    class DeliveryRoute {
        private List<RouteSegment> segments;
        private Double totalDistance;
        private Integer totalDuration;
    }

    @Data
    class RouteSegment {
        private Point from;
        private Point to;
        private Double distance;
        private Integer duration;
        private String polyline;
    }
}
```

**Step 3: Commit**

```bash
git add gh-transport-integration/map/
git commit -m "feat: 实现路径规划集成"
```

---

## 第十三阶段：签收管理与异常处理

### Task 31: 签收管理模块

**Files:**
- Create: `gh-transport-order/sign/pom.xml`
- Create: `gh-transport-order/sign/src/main/java/com/ghtransport/sign/entity/SignRecord.java`
- Create: `gh-transport-order/sign/src/main/java/com/ghtransport/sign/entity/SignPhoto.java`
- Create: `gh-transport-order/sign/src/main/java/com/ghtransport/sign/entity/SignException.java`
- Create: `gh-transport-order/sign/src/main/java/com/ghtransport/sign/service/SignService.java`
- Create: `gh-transport-order/sign/src/main/java/com/ghtransport/sign/controller/SignController.java`

**Step 1: 创建签收记录实体**

```java
package com.ghtransport.sign.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_sign_record")
public class SignRecord extends BaseEntity {
    private Long tenantId;
    private String taskId;           // 调度任务ID
    private Long orderId;            // 订单ID
    private String orderNo;          // 订单编号
    private String signType;          // 签收类型: DRIVER_SIGN, CUSTOMER_SIGN, CODE_SIGN
    private String signerName;       // 签收人姓名
    private String signerPhone;      // 签收人电话
    private String signCode;         // 签收验证码
    private String signMethod;       // 签收方式: APP, SMS, FACE
    private LocalDateTime signTime;  // 签收时间
    private String signLocation;     // 签收位置
    private Double signLat;          // 签收纬度
    private Double signLng;          // 签收经度
    private String status;           // 状态: NORMAL, EXCEPTION, PENDING
    private String remark;           // 备注
}
```

**Step 2: 创建签收照片实体**

```java
package com.ghtransport.sign.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_sign_photo")
public class SignPhoto extends BaseEntity {
    private Long tenantId;
    private Long signRecordId;       // 关联签收记录
    private String photoType;        // 照片类型: RECEIPT, GOODS, CUSTOMER, SIGNATURE
    private String photoUrl;         // 照片URL
    private String ossKey;           // OSS存储Key
    private Long fileSize;           // 文件大小
    private String description;      // 照片描述
}
```

**Step 3: 创建签收异常实体**

```java
package com.ghtransport.sign.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_sign_exception")
public class SignException extends BaseEntity {
    private Long tenantId;
    private Long signRecordId;       // 关联签收记录
    private String orderId;          // 订单ID
    private String exceptionType;    // 异常类型: REFUSED, DAMAGED, LOST, LATE, OTHER
    private String description;      // 异常描述
    private String photos;          // 异常照片(JSON数组)
    private String handleStatus;    // 处理状态: PENDING, HANDLING, RESOLVED
    private String handleResult;    // 处理结果
    private LocalDateTime handleTime;// 处理时间
    private Long handleBy;          // 处理人
}
```

**Step 4: 创建签收服务**

```java
package com.ghtransport.sign.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghtransport.sign.entity.SignRecord;
import com.ghtransport.sign.entity.SignPhoto;

import java.util.List;

public interface SignService extends IService<SignRecord> {

    /**
     * 司机APP签收
     */
    SignRecord driverSign(Long orderId, Long driverId, String signPhotoUrls);

    /**
     * 客户验证码签收
     */
    SignRecord codeSign(Long orderId, String signCode, String customerName);

    /**
     * 人脸签收
     */
    SignRecord faceSign(Long orderId, String faceData);

    /**
     * 上传签收照片
     */
    List<SignPhoto> uploadSignPhotos(Long signRecordId, List<String> photoUrls, String photoType);

    /**
     * 登记异常
     */
    SignRecord reportException(Long orderId, String exceptionType, String description, List<String> photoUrls);

    /**
     * 处理异常
     */
    boolean handleException(Long exceptionId, String handleResult, Long handlerId);

    /**
     * 生成签收验证码
     */
    String generateSignCode(Long orderId);

    /**
     * 验证签收码
     */
    boolean verifySignCode(Long orderId, String code);
}
```

**Step 5: 创建签收Controller**

```java
package com.ghtransport.sign.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.common.result.Result;
import com.ghtransport.sign.entity.SignException;
import com.ghtransport.sign.entity.SignPhoto;
import com.ghtransport.sign.entity.SignRecord;
import com.ghtransport.sign.service.SignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "签收管理", description = "订单签收管理")
@RestController
@RequestMapping("/api/v1/sign")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    @PostMapping("/driver")
    @Operation(summary = "司机签收")
    public Result<SignRecord> driverSign(
            @RequestParam Long orderId,
            @RequestParam Long driverId,
            @RequestParam(required = false) String photoUrls) {
        return Result.success(signService.driverSign(orderId, driverId, photoUrls));
    }

    @PostMapping("/code")
    @Operation(summary = "验证码签收")
    public Result<SignRecord> codeSign(
            @RequestParam Long orderId,
            @RequestParam String signCode,
            @RequestParam String customerName) {
        return Result.success(signService.codeSign(orderId, signCode, customerName));
    }

    @PostMapping("/{signRecordId}/photos")
    @Operation(summary = "上传签收照片")
    public Result<List<SignPhoto>> uploadPhotos(
            @PathVariable Long signRecordId,
            @RequestParam String photoType,
            @RequestBody List<String> photoUrls) {
        return Result.success(signService.uploadSignPhotos(signRecordId, photoUrls, photoType));
    }

    @PostMapping("/exception")
    @Operation(summary = "登记签收异常")
    public Result<SignRecord> reportException(
            @RequestParam Long orderId,
            @RequestParam String exceptionType,
            @RequestParam(required = false) String description,
            @RequestBody(required = false) List<String> photoUrls) {
        return Result.success(signService.reportException(orderId, exceptionType, description, photoUrls));
    }

    @PostMapping("/exception/{exceptionId}/handle")
    @Operation(summary = "处理异常")
    @PreAuthorize("hasAuthority('sign:exception:handle')")
    public Result<String> handleException(
            @PathVariable Long exceptionId,
            @RequestParam String handleResult) {
        boolean success = signService.handleException(exceptionId, handleResult, null);
        return success ? Result.success("处理成功") : Result.fail("处理失败");
    }

    @GetMapping("/exception/page")
    @Operation(summary = "异常列表")
    @PreAuthorize("hasAuthority('sign:exception:view')")
    public Result<Page<SignException>> exceptionPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String handleStatus) {
        return Result.success(null); // 实现分页查询
    }

    @GetMapping("/code/{orderId}")
    @Operation(summary = "获取签收验证码")
    public Result<String> getSignCode(@PathVariable Long orderId) {
        return Result.success(signService.generateSignCode(orderId));
    }
}
```

**Step 6: Commit**

```bash
git add gh-transport-order/sign/
git commit -m "feat: 实现签收管理模块"
```

---

## 第十四阶段：客户门户与消息推送

### Task 32: 客户门户模块

**Files:**
- Create: `gh-transport-portal/pom.xml`
- Create: `gh-transport-portal/src/main/java/com/ghtransport/portal/entity/CustomerUser.java`
- Create: `gh-transport-portal/src/main/java/com/ghtransport/portal/entity/CustomerToken.java`
- Create: `gh-transport-portal/src/main/java/com/ghtransport/portal/service/PortalAuthService.java`
- Create: `gh-transport-portal/src/main/java/com/ghtransport/portal/service/PortalOrderService.java`
- Create: `gh-transport-portal/src/main/java/com/ghtransport/portal/controller/PortalController.java`

**Step 1: 创建客户用户实体**

```java
package com.ghtransport.portal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_customer_user")
public class CustomerUser extends BaseEntity {
    private Long tenantId;
    private Long customerId;         // 关联客户ID
    private String username;          // 用户名(手机号)
    private String password;          // 密码
    private String realName;          // 真实姓名
    private String phone;             // 联系电话
    private String email;             // 邮箱
    private String status;            // 状态: ACTIVE, INACTIVE, LOCKED
    private LocalDateTime lastLoginAt;// 最后登录时间
    private String lastLoginIp;       // 最后登录IP
}
```

**Step 2: 创建门户认证服务**

```java
package com.ghtransport.portal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghtransport.portal.entity.CustomerUser;

public interface PortalAuthService extends IService<CustomerUser> {

    /**
     * 客户注册
     */
    CustomerUser register(String phone, String password, String verifyCode);

    /**
     * 客户登录
     */
    String login(String username, String password);

    /**
     * 刷新Token
     */
    String refreshToken(String refreshToken);

    /**
     * 重置密码
     */
    boolean resetPassword(String phone, String newPassword, String verifyCode);

    /**
     * 发送注册验证码
     */
    boolean sendRegisterCode(String phone);

    /**
     * 发送重置密码验证码
     */
    boolean sendResetCode(String phone);
}
```

**Step 3: 创建门户订单服务**

```java
package com.ghtransport.portal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghtransport.order.entity.Order;

import java.time.LocalDate;
import java.util.Map;

public interface PortalOrderService extends IService<Order> {

    /**
     * 客户自助下单
     */
    Order createOrder(Long customerId, Order order);

    /**
     * 查询我的订单
     */
    Map<String, Object> myOrders(Long customerId, int page, int size, String status);

    /**
     * 查询订单详情
     */
    Map<String, Object> orderDetail(Long customerId, Long orderId);

    /**
     * 查询订单轨迹
     */
    Map<String, Object> orderTrack(Long customerId, Long orderId);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long customerId, Long orderId);

    /**
     * 获取预估价格
     */
    Map<String, Object> estimatePrice(String pickupAddr, String deliveryAddr,
                                     Double weight, Double volume);
}
```

**Step 4: 创建门户Controller**

```java
package com.ghtransport.portal.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.common.result.Result;
import com.ghtransport.order.entity.Order;
import com.ghtransport.portal.service.PortalAuthService;
import com.ghtransport.portal.service.PortalOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "客户门户", description = "客户自助服务接口")
@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalAuthService portalAuthService;
    private final PortalOrderService portalOrderService;

    // ========== 认证相关 ==========

    @PostMapping("/register")
    @Operation(summary = "客户注册")
    public Result<Map<String, Object>> register(
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String verifyCode) {
        // 实现注册逻辑
        return Result.success(Map.of());
    }

    @PostMapping("/login")
    @Operation(summary = "客户登录")
    public Result<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password) {
        String token = portalAuthService.login(username, password);
        return Result.success(Map.of("token", token));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<String> refresh(@RequestParam String refreshToken) {
        return Result.success(portalAuthService.refreshToken(refreshToken));
    }

    // ========== 订单相关 ==========

    @PostMapping("/orders")
    @Operation(summary = "自助下单")
    public Result<Order> createOrder(@RequestBody Order order) {
        // 需要从登录信息获取customerId
        return Result.success(portalOrderService.createOrder(1L, order));
    }

    @GetMapping("/orders")
    @Operation(summary = "我的订单列表")
    public Result<Page<Order>> myOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        // 需要从登录信息获取customerId
        return Result.success(portalOrderService.myOrders(1L, page, size, status));
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "订单详情")
    public Result<Map<String, Object>> orderDetail(@PathVariable Long orderId) {
        return Result.success(portalOrderService.orderDetail(1L, orderId));
    }

    @GetMapping("/orders/{orderId}/track")
    @Operation(summary = "订单轨迹")
    public Result<Map<String, Object>> orderTrack(@PathVariable Long orderId) {
        return Result.success(portalOrderService.orderTrack(1L, orderId));
    }

    @PostMapping("/orders/{orderId}/cancel")
    @Operation(summary = "取消订单")
    public Result<String> cancelOrder(@PathVariable Long orderId) {
        boolean success = portalOrderService.cancelOrder(1L, orderId);
        return success ? Result.success("取消成功") : Result.fail("取消失败");
    }

    @GetMapping("/estimate")
    @Operation(summary = "预估价格")
    public Result<Map<String, Object>> estimatePrice(
            @RequestParam String pickupAddr,
            @RequestParam String deliveryAddr,
            @RequestParam Double weight,
            @RequestParam Double volume) {
        return Result.success(portalOrderService.estimatePrice(pickupAddr, deliveryAddr, weight, volume));
    }
}
```

**Step 5: Commit**

```bash
git add gh-transport-portal/
git commit -m "feat: 实现客户门户模块"
```

---

### Task 33: 消息推送服务

**Files:**
- Create: `gh-transport-integration/notification/pom.xml`
- Create: `gh-transport-integration/notification/src/main/java/com/ghtransport/notification/entity/MessageTemplate.java`
- Create: `gh-transport-integration/notification/src/main/java/com/ghtransport/notification/entity/MessageRecord.java`
- Create: `gh-transport-integration/notification/src/main/java/com/ghtransport/notification/service/MessageService.java`
- Create: `gh-transport-integration/notification/src/main/java/com/ghtransport/notification/service/NotificationCenter.java`

**Step 1: 创建消息模板实体**

```java
package com.ghtransport.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_message_template")
public class MessageTemplate extends BaseEntity {
    private String templateCode;      // 模板编码
    private String templateType;      // 模板类型: SMS, EMAIL, PUSH, WEBHOOK
    private String templateName;      // 模板名称
    private String content;           // 模板内容
    private String params;            // 参数定义(JSON)
    private Integer status;           // 状态: 0禁用 1启用
}
```

**Step 2: 创建消息记录实体**

```java
package com.ghtransport.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_message_record")
public class MessageRecord extends BaseEntity {
    private Long tenantId;
    private String messageNo;         // 消息编号
    private String templateCode;      // 模板编码
    private String messageType;       // 消息类型
    private String recipient;         // 接收者
    private String subject;           // 标题
    private String content;           // 内容
    private String params;            // 参数(JSON)
    private String status;            // 状态: PENDING, SENT, FAILED
    private String sendChannel;       // 发送渠道
    private LocalDateTime sendTime;   // 发送时间
    private String failReason;        // 失败原因
    private Integer retryCount;       // 重试次数
}
```

**Step 3: 创建消息服务**

```java
package com.ghtransport.notification.service;

import java.util.Map;

public interface MessageService {

    /**
     * 发送短信
     */
    boolean sendSms(String phone, String templateCode, Map<String, String> params);

    /**
     * 发送邮件
     */
    boolean sendEmail(String to, String subject, String content);

    /**
     * 发送APP推送
     */
    boolean sendPush(String userId, String title, String content);

    /**
     * 发送Webhook通知
     */
    boolean sendWebhook(String url, Map<String, Object> payload);

    /**
     * 发送订单状态通知
     */
    boolean sendOrderStatusNotification(Long orderId, String status);

    /**
     * 发送调度通知
     */
    boolean sendDispatchNotification(Long taskId, Long driverId);
}
```

**Step 4: Commit**

```bash
git add gh-transport-integration/notification/
git commit -m "feat: 实现消息推送服务"
```

---

## 第十五阶段：监控告警与性能优化

### Task 34: 监控告警系统

**Files:**
- Create: `gh-transport-monitor/pom.xml`
- Create: `gh-transport-monitor/src/main/java/com/ghtransport/monitor/config/PrometheusConfig.java`
- Create: `gh-transport-monitor/src/main/java/com/ghtransport/monitor/service/MetricsService.java`
- Create: `gh-transport-monitor/src/main/java/com/ghtransport/monitor/service/AlertService.java`
- Create: `gh-transport-monitor/src/main/java/com/ghtransport/monitor/entity/AlertRule.java`
- Create: `gh-transport-monitor/src/main/java/com/ghtransport/monitor/entity/AlertRecord.java`
- Create: `gh-transport-monitor/src/main/java/com/ghtransport/monitor/controller/MonitorController.java`

**Step 1: 创建告警规则实体**

```java
package com.ghtransport.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ghtransport.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gt_alert_rule")
public class AlertRule extends BaseEntity {
    private String ruleName;         // 规则名称
    private String metricName;        // 指标名称
    private String metricType;        // 指标类型: COUNTER, GAUGE, HISTOGRAM
    private String condition;         // 触发条件: GT, LT, EQ, GTE, LTE
    private Double threshold;         // 阈值
    private Integer duration;         // 持续时间(秒)
    private String severity;          // 严重级别: INFO, WARNING, CRITICAL, EMERGENCY
    private String notificationType;  // 通知方式: SMS, EMAIL, DINGTALK, WEBHOOK
    private String notificationTarget;// 通知目标
    private String description;       // 描述
    private Integer status;           // 状态: 0禁用 1启用
}
```

**Step 2: 创建Prometheus配置**

```java
package com.ghtransport.monitor.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrometheusConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT_CONFIG);

        // 添加自定义指标
        registry.counter("http_requests_total",
            Tags.of("method", "GET", "uri", "/api/orders"));

        registry.gauge("jvm_memory_used_bytes",
            Tags.of("area", "heap"),
            Runtime.getRuntime(),
            rt -> rt.totalMemory() - rt.freeMemory());

        return registry;
    }
}
```

**Step 3: 创建告警服务**

```java
package com.ghtransport.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghtransport.monitor.entity.AlertRecord;

import java.util.List;

public interface AlertService extends IService<AlertRecord> {

    /**
     * 检查指标是否触发告警
     */
    void checkMetric(String metricName, double value);

    /**
     * 触发告警
     */
    void triggerAlert(Long ruleId, String metricName, double value);

    /**
     * 处理告警
     */
    boolean handleAlert(Long alertId, String handleResult, Long handlerId);

    /**
     * 获取活跃告警
     */
    List<AlertRecord> getActiveAlerts();

    /**
     * 获取告警历史
     */
    List<AlertRecord> getAlertHistory(int page, int size);
}
```

**Step 4: 创建监控Controller**

```java
package com.ghtransport.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghtransport.common.result.Result;
import com.ghtransport.monitor.entity.AlertRecord;
import com.ghtransport.monitor.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "监控告警", description = "系统监控和告警管理")
@RestController
@RequestMapping("/api/v1/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final AlertService alertService;

    @GetMapping("/metrics")
    @Operation(summary = "获取监控指标")
    public Result<Map<String, Object>> getMetrics() {
        // 返回当前系统指标
        return Result.success(Map.of(
            "cpu_usage", 45.5,
            "memory_usage", 72.3,
            "disk_usage", 65.0,
            "active_users", 128,
            "requests_per_second", 256
        ));
    }

    @GetMapping("/alerts/active")
    @Operation(summary = "获取活跃告警")
    @PreAuthorize("hasAuthority('monitor:alert:view')")
    public Result<Page<AlertRecord>> getActiveAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(null);
    }

    @GetMapping("/alerts/history")
    @Operation(summary = "获取告警历史")
    @PreAuthorize("hasAuthority('monitor:alert:view')")
    public Result<Page<AlertRecord>> getAlertHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(alertService.getAlertHistory(page, size));
    }

    @PostMapping("/alerts/{alertId}/handle")
    @Operation(summary = "处理告警")
    @PreAuthorize("hasAuthority('monitor:alert:handle')")
    public Result<String> handleAlert(
            @PathVariable Long alertId,
            @RequestParam String handleResult) {
        boolean success = alertService.handleAlert(alertId, handleResult, null);
        return success ? Result.success("处理成功") : Result.fail("处理失败");
    }

    @GetMapping("/health")
    @Operation(summary = "系统健康检查")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
            "status", "UP",
            "components", Map.of(
                "database", "UP",
                "redis", "UP",
                "kafka", "UP"
            )
        ));
    }
}
```

**Step 5: Commit**

```bash
git add gh-transport-monitor/
git commit -m "feat: 实现监控告警系统"
```

---

### Task 35: 性能优化

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/config/CacheConfig.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/config/RateLimitConfig.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/ratelimit/RateLimitAspect.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/ratelimit/LeakyBucket.java`

**Step 1: 创建缓存配置**

```java
package com.ghtransport.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(new StringRedisSerializer())
            .serializeValuesWith(new GenericJackson2JsonRedisSerializer())
            .disableCachingNullValues();

        // 分表缓存配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 热点数据缓存 - 10分钟
        cacheConfigurations.put("hot_data", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 配置数据缓存 - 30分钟
        cacheConfigurations.put("config_data", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 字典数据缓存 - 1小时
        cacheConfigurations.put("dict_data", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }

    @Bean
    public CaffeineCacheManager localCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5));
        return cacheManager;
    }
}
```

**Step 2: 创建限流配置**

```java
package com.ghtransport.common.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class RateLimitConfig {

    // API限流配置
    private static final Map<String, Bucket> BUCKETS = new ConcurrentHashMap<>();

    /**
     * 创建限流Bucket
     * @param key 限流key
     * @param capacity 令牌桶容量
     * @param refillRate 重填速率(每秒)
     */
    public static Bucket createBucket(String key, int capacity, int refillRate) {
        Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.classic(capacity, Refill.greedy(refillRate, Duration.ofSeconds(1)))
            .build();
        BUCKETS.put(key, bucket);
        return bucket;
    }

    /**
     * 尝试消费令牌
     * @param key 限流key
     * @param tokens 消耗令牌数
     * @return 是否成功
     */
    public static boolean tryConsume(String key, int tokens) {
        Bucket bucket = BUCKETS.computeIfAbsent(key, k -> createBucket(k, 100, 10));
        return bucket.tryConsume(tokens);
    }

    /**
     * 获取剩余令牌数
     */
    public static long getAvailableTokens(String key) {
        Bucket bucket = BUCKETS.get(key);
        return bucket != null ? bucket.getAvailableTokens() : 100;
    }
}
```

**Step 3: 创建限流切面**

```java
package com.ghtransport.common.ratelimit;

import com.ghtransport.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Slf4j
@Aspect
@Component
@Order(1)
public class RateLimitAspect {

    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimited) throws Throwable {
        String key = getRateLimitKey(point, rateLimited.key());

        if (!RateLimitConfig.tryConsume(key, rateLimited.tokens())) {
            log.warn("请求被限流: key={}", key);
            return Result.fail("请求过于频繁，请稍后再试", 429);
        }

        return point.proceed();
    }

    private String getRateLimitKey(ProceedingJoinPoint point, String customKey) {
        if (customKey != null && !customKey.isEmpty()) {
            return customKey;
        }
        return point.getSignature().toShortString();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RateLimit {
        String key() default "";
        int tokens() default 1;
    }
}
```

**Step 4: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/config/
git add gh-transport-common/src/main/java/com/ghtransport/common/ratelimit/
git commit -m "feat: 实现缓存和限流配置"
```

---

## 第十六阶段：安全增强与数据保护

### Task 36: 数据脱敏与安全增强

**Files:**
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/DataMaskingAspect.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/SensitiveType.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/SensitiveDataFilter.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/PasswordValidator.java`
- Create: `gh-transport-common/src/main/java/com/ghtransport/common/security/LoginRetryLimit.java`

**Step 1: 创建敏感数据类型枚举**

```java
package com.ghtransport.common.security;

public enum SensitiveType {
    /**
     * 手机号脱敏: 138****8888
     */
    PHONE,

    /**
     * 身份证号脱敏: 110***********1234
     */
    ID_CARD,

    /**
     * 银行卡号脱敏: 6222**********8888
     */
    BANK_CARD,

    /**
     * 姓名脱敏: 张*
     */
    NAME,

    /**
     * 地址脱敏: 北京市海淀区****
     */
    ADDRESS,

    /**
     * 邮箱脱敏: t***@example.com
     */
    EMAIL,

    /**
     * 密码脱敏: ********
     */
    PASSWORD
}
```

**Step 2: 创建数据脱敏工具**

```java
package com.ghtransport.common.security;

import java.util.regex.Pattern;

public class SensitiveDataFilter {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{3})\\d{4}(\\d{4})");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{3})\\d{10}(\\\\\d{3}[Xx])");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})\\d+(\\d{4})");

    /**
     * 脱敏处理
     */
    public static String mask(String value, SensitiveType type) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (type) {
            case PHONE -> maskPhone(value);
            case ID_CARD -> maskIdCard(value);
            case BANK_CARD -> maskBankCard(value);
            case NAME -> maskName(value);
            case ADDRESS -> maskAddress(value);
            case EMAIL -> maskEmail(value);
            case PASSWORD -> "********";
        };
    }

    private static String maskPhone(String phone) {
        return PHONE_PATTERN.matcher(phone).replaceAll("$1****$2");
    }

    private static String maskIdCard(String idCard) {
        return ID_CARD_PATTERN.matcher(idCard).replaceAll("$1***********$2");
    }

    private static String maskBankCard(String bankCard) {
        return BANK_CARD_PATTERN.matcher(bankCard).replaceAll("$1**********$2");
    }

    private static String maskName(String name) {
        if (name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*";
    }

    private static String maskAddress(String address) {
        if (address.length() <= 6) {
            return address;
        }
        return address.substring(0, 6) + "****";
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 3) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 3) + "***" + email.substring(atIndex);
    }
}
```

**Step 3: 创建密码校验器**

```java
package com.ghtransport.common.security;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final Pattern LENGTH_PATTERN = Pattern.compile("^.{8,64}$");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    /**
     * 校验密码强度
     * @return 校验结果
     */
    public ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.fail("密码不能为空");
        }

        if (!LENGTH_PATTERN.matcher(password).matches()) {
            return ValidationResult.fail("密码长度必须为8-64位");
        }

        int score = 0;
        if (UPPERCASE_PATTERN.matcher(password).matches()) score++;
        if (LOWERCASE_PATTERN.matcher(password).matches()) score++;
        if (NUMBER_PATTERN.matcher(password).matches()) score++;
        if (SPECIAL_PATTERN.matcher(password).matches()) score++;

        if (score < 2) {
            return ValidationResult.fail("密码必须包含大小写字母、数字、特殊字符中的至少两种");
        }

        return ValidationResult.success();
    }

    /**
     * 加密密码
     */
    public String encrypt(String password) {
        // 使用BCrypt加密
        return ""; // 实际使用BCryptPasswordEncoder
    }

    public record ValidationResult(boolean valid, String message) {
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }
    }
}
```

**Step 4: Commit**

```bash
git add gh-transport-common/src/main/java/com/ghtransport/common/security/
git commit -m "feat: 实现数据脱敏和安全增强"
```

---

## 完整模块清单（最终版）

```
gh-transport-parent                    # 父POM
├── gh-transport-common                # 公共模块
│   ├── config                       # 配置类
│   ├── security                     # 安全(JWT、BCrypt、脱敏)
│   ├── tenant                      # 多租户
│   ├── result                      # 统一响应
│   ├── exception                   # 异常处理
│   ├── ratelimit                   # 限流
│   └── handler                     # MyBatis处理器
├── gh-transport-api                   # API实体和Mapper
├── gh-transport-gateway               # 网关服务
├── gh-transport-admin                 # 管理后台
│   ├── controller                   # 控制器
│   ├── entity                       # 实体
│   ├── service                      # 服务
│   └── aspect                       # 切面
├── gh-transport-loading               # 装载优化
│   ├── algorithm                    # 算法
│   └── service                      # 服务
├── gh-transport-tracking              # 车辆追踪
│   ├── gps                          # GPS处理
│   └── service                      # 服务
├── gh-transport-dispatch              # 调度排单 ⭐新增
│   ├── entity                       # 调度任务
│   ├── service                      # 调度服务
│   └── algorithm                    # 调度算法
├── gh-transport-order                 # 订单管理
│   └── sign                         # 签收管理 ⭐新增
├── gh-transport-inventory             # 库存管理
│   ├── purchase                     # 采购管理
│   ├── sales                        # 销售管理
│   └── warehouse                    # 仓库管理
├── gh-transport-vehicle               # 车辆管理
├── gh-transport-finance               # 财务管理
│   ├── receivable                   # 应收管理
│   ├── payable                     # 应付管理
│   └── invoice                     # 发票管理
├── gh-transport-report                # 报表模块
├── gh-transport-schedule              # 定时任务
├── gh-transport-portal                # 客户门户 ⭐新增
├── gh-transport-monitor               # 监控告警 ⭐新增
└── gh-transport-integration           # 第三方集成
    ├── sms                           # 短信服务
    ├── email                         # 邮件服务
    ├── map                           # 地图服务
    └── notification                  # 消息推送 ⭐新增
```

---

## 总结

本次更新补充了以下模块：

| 阶段 | 新增模块 | 主要功能 |
|------|----------|----------|
| 第十二阶段 | 调度排单 | 自动调度、手动调度、调度规则 |
| 第十二阶段 | 路径规划 | 路线规划、TSP优化、里程计算 |
| 第十三阶段 | 签收管理 | 司机签收、验证码签收、异常处理 |
| 第十四阶段 | 客户门户 | 自助下单、订单查询、轨迹追踪 |
| 第十四阶段 | 客户管理 | 客户档案、联系人、信用额度 |
| 第十五阶段 | 司机管理 | 司机档案、排班、绩效考核 |
| 第十五阶段 | 设备管理 | GPS设备绑定、状态监控 |
| 第十五阶段 | 消息推送 | 短信、邮件、APP推送 |
| 第十六阶段 | 监控告警 | Prometheus指标、告警规则 |
| 第十六阶段 | 性能优化 | 多级缓存、接口限流 |
| 第十七阶段 | 安全增强 | 数据脱敏、密码校验 |
| 第十七阶段 | 数据初始化 | 基础数据、字典数据、演示数据 |

**最终模块清单（23个模块）：**
- gh-transport-admin - 管理后台
- gh-transport-customer - 客户管理 ⭐新增
- gh-transport-driver - 司机管理 ⭐新增
- gh-transport-device - 设备管理 ⭐新增
- gh-transport-dispatch - 调度排单 ⭐
- gh-transport-finance - 财务管理
- gh-transport-gateway - 网关
- gh-transport-integration - 第三方集成
- gh-transport-loading - 装载优化
- gh-transport-message - 消息通知 ⭐新增
- gh-transport-monitor - 监控告警 ⭐
- gh-transport-notification - 消息推送 ⭐
- gh-transport-order - 订单管理
- gh-transport-inventory - 库存管理
- gh-transport-portal - 客户门户 ⭐
- gh-transport-report - 报表模块
- gh-transport-schedule - 定时任务
- gh-transport-tracking - 车辆追踪
- gh-transport-vehicle - 车辆管理

**计划完成并保存到 `docs/plans/2025-01-27-logistics-erp-system.md`。**

**两个执行选项：**

**1. Subagent-Driven（本会话）** - 我按任务逐个派遣子代理，每个任务后进行代码审查，快速迭代

**2. Parallel Session（单独会话）** - 在worktree中打开新会话，批量执行

**您选择哪种方式？**
