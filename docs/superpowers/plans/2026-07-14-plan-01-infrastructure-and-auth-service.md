# Plan 1: 基础设施 + Auth Service 迁移

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建微服务项目骨架，Nacos 注册中心就绪，auth-service 独立运行并通过 Gateway 可访问。

**Architecture:** 从原 cloud-mall 单体仓库复制代码，按设计方案拆分为多模块 Maven 项目。第一阶段只迁移 auth 模块，验证服务注册发现和网关路由链路。

**Tech Stack:** Spring Boot 3.5.7, Spring Cloud 2024.x, Nacos, OpenFeign, MyBatis-Plus, JWT, MySQL

## Global Constraints

- Java 17, Maven
- Spring Boot 3.5.7 (parent pom)，Spring Cloud 2024.x (BOM)
- MySQL 数据库按服务独立（cm_auth）
- MyBatis-Plus 3.5.14, Hutool 5.8.42, JJWT 0.13.0
- Request/Response/Client 命名约定
- 禁止通配符 import
- 代码风格遵循原项目 code-style.md（4 空格缩进，K&R 大括号等）

---

### Task 1: 创建父 POM + 目录结构

**Files:**
- Create: `pom.xml`
- Create: `common/pom.xml`
- Create: `api/pom.xml`
- Create: `gateway/pom.xml`
- Create: `auth-service/pom.xml`
- Create: `auth-service/auth-api/pom.xml`
- Create: `auth-service/auth-biz/pom.xml`
- Create: `.gitignore`
- Create: `docs/docker/docker-compose-infra.yml`

**Interfaces:**
- Consumes: 无
- Produces: 可编译的多模块 Maven 项目骨架

- [ ] **Step 1: 创建父 POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.7</version>
    </parent>

    <groupId>com.cloudmall</groupId>
    <artifactId>cloud-mall-cloud</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <modules>
        <module>common</module>
        <module>api</module>
        <module>gateway</module>
        <module>auth-service</module>
    </modules>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-cloud.version>2024.0.1</spring-cloud.version>
        <mybatis-plus-version>3.5.14</mybatis-plus-version>
        <hutool-version>5.8.42</hutool-version>
        <jjwt-version>0.13.0</jjwt-version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-loadbalancer</artifactId>
            </dependency>
            <!-- 项目内部模块 -->
            <dependency>
                <groupId>com.cloudmall</groupId>
                <artifactId>common</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
            <dependency>
                <groupId>com.cloudmall</groupId>
                <artifactId>api</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
            <dependency>
                <groupId>com.cloudmall</groupId>
                <artifactId>auth-api</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

- [ ] **Step 2: 创建目录结构**

Run:
```bash
mkdir -p cloud-mall-cloud/common/src/main/java/com/cloudmall/common/{entity,enums,exception,utils,config}
mkdir -p cloud-mall-cloud/api/src/main/java/com/cloudmall/api/auth
mkdir -p cloud-mall-cloud/gateway/src/main/java/com/cloudmall/gateway/{config,filter}
mkdir -p cloud-mall-cloud/auth-service/auth-api/src/main/java/com/cloudmall/auth/api/{request,response,client}
mkdir -p cloud-mall-cloud/auth-service/auth-biz/src/main/java/com/cloudmall/auth/{controller,service/impl,security/filter,config,mapper,entity}
mkdir -p cloud-mall-cloud/auth-service/auth-biz/src/main/resources
mkdir -p cloud-mall-cloud/docs/docker
mkdir -p cloud-mall-cloud/docs/sql
```

- [ ] **Step 3: 创建 .gitignore**

```gitignore
target/
*.iml
.idea/
*.class
*.jar
*.log
logs/
application-local.yml
```

- [ ] **Step 4: 创建 common/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>common</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool-version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 5: 创建 api/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/...">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>api</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.cloudmall</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 6: 创建 auth-api/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/...">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <groupId>com.cloudmall</groupId>
    <artifactId>auth-api</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.cloudmall</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 7: 创建 auth-biz 和 gateway pom.xml（略，继承父 POM，依赖对应 api 和 common）**

- [ ] **Step 8: 编译验证**

Run:
```bash
mvn clean compile -DskipTests
Expected: BUILD SUCCESS
```

- [ ] **Step 9: Commit**

```bash
git init && git add . && git commit -m "chore: init multi-module cloud-mall-cloud project skeleton"
```

---

### Task 2: common 模块 — 基础设施代码

**Files:**
- Create: `common/src/main/java/com/cloudmall/common/entity/Result.java`
- Create: `common/src/main/java/com/cloudmall/common/entity/PageResult.java`
- Create: `common/src/main/java/com/cloudmall/common/enums/BizErrorCode.java`
- Create: `common/src/main/java/com/cloudmall/common/exception/BizException.java`
- Create: `common/src/main/java/com/cloudmall/common/utils/AssertUtils.java`
- Create: `common/src/main/java/com/cloudmall/common/context/AuthUserContext.java`

**Interfaces:**
- Produces: `Result<T>`, `PageResult<T>`, `BizErrorCode`, `BizException`, `AssertUtils`, `AuthUserContext`

- [ ] **Step 1: 从原项目复制通用类**

从 `cloud-mall/cloud-mall-framework/src/main/java/com/cloudmall/framework/` 复制到 `common/src/main/java/com/cloudmall/common/`，调整包路径：

- `common/entity/Result.java` → `com.cloudmall.common.entity.Result`
- `common/entity/PageResult.java` → `com.cloudmall.common.entity.PageResult`
- `common/enums/BizErrorCode.java` → `com.cloudmall.common.enums.BizErrorCode`
- `common/exception/BizException.java` → `com.cloudmall.common.exception.BizException`
- `common/utils/AssertUtils.java` → `com.cloudmall.common.utils.AssertUtils`

**关键调整：** 将所有 `com.cloudmall.framework` 包引用改为 `com.cloudmall.common`

- [ ] **Step 2: 创建 AuthUserContext**

从原项目 `models/auth/bo/AuthUser.java` 抽出精简版：

```java
package com.cloudmall.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserContext {
    private Long userId;
    private String username;
    private Long storeId;
    private String userType; // "web" | "manager" | "merchant"

    public static AuthUserContext fromAuthUser(Object authUser) {
        // 反射从 AuthUser 对象中取字段，各服务自己实现具体转换
        return AuthUserContext.builder().build();
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn clean compile -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add common/ && git commit -m "feat(common): add base infrastructure classes"
```

---

### Task 3: api 模块 + auth-api 模块

**Files:**
- Create: `api/src/main/java/com/cloudmall/api/auth/request/LoginRequest.java`
- Create: `api/src/main/java/com/cloudmall/api/auth/response/LoginResponse.java`
- Create: `api/src/main/java/com/cloudmall/api/auth/response/UserInfoResponse.java`
- Create: `auth-service/auth-api/src/main/java/com/cloudmall/auth/api/request/RegisterRequest.java`
- Create: `auth-service/auth-api/src/main/java/com/cloudmall/auth/api/client/AuthClient.java`

**Interfaces:**
- Produces: `LoginRequest`, `LoginResponse`, `UserInfoResponse`, `RegisterRequest`, `AuthClient`
- Consumes: `Result` (from common)

- [ ] **Step 1: 创建通用 api 的 DTO**

```java
// LoginRequest.java
package com.cloudmall.api.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "用户类型不能为空")
    private String userType; // "web" | "manager" | "merchant"
}
```

```java
// LoginResponse.java
package com.cloudmall.api.auth.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String userType;
}
```

```java
// UserInfoResponse.java
package com.cloudmall.api.auth.response;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long storeId;
    private String userType;
}
```

- [ ] **Step 2: 创建 auth-api 的 AuthClient**

```java
package com.cloudmall.auth.api.client;

import com.cloudmall.api.auth.request.LoginRequest;
import com.cloudmall.api.auth.response.LoginResponse;
import com.cloudmall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthClient {

    @PostMapping("/login")
    Result<LoginResponse> login(@RequestBody LoginRequest request);
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn clean compile -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add api/ auth-service/auth-api/ && git commit -m "feat(api): add auth api contracts with Request/Response/Client"
```

---

### Task 4: Nacos Docker Compose

**Files:**
- Create: `docs/docker/docker-compose-infra.yml`
- Create: `docs/sql/cm_auth.sql`

**Interfaces:**
- Produces: 本地运行的 Nacos 服务

- [ ] **Step 1: 创建 Nacos + MySQL Docker Compose**

```yaml
version: '3.8'
services:
  nacos:
    image: nacos/nacos-server:v2.5.0
    container_name: cloud-mall-nacos
    ports:
      - "8848:8848"
      - "9848:9848"
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_PORT: 3306
      MYSQL_SERVICE_DB_NAME: cm_nacos
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: root123
    depends_on:
      mysql:
        condition: service_healthy

  mysql:
    image: mysql:8.4
    container_name: cloud-mall-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root123
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

- [ ] **Step 2: 创建 auth 数据库建表脚本**

```sql
-- docs/sql/cm_auth.sql
CREATE DATABASE IF NOT EXISTS cm_auth DEFAULT CHARACTER SET utf8mb4;

USE cm_auth;

-- 从原项目复制 auth_user 表的 DDL
-- 原表位于 cloud-mall/cloud-mall-framework/src/main/resources/script/ 或 docs/sql/
```

从原项目复制建表 DDL。文件位于原项目 `cloud-mall/docs/sql/`（查找 `auth` 或 `user` 相关表结构），以及原项目 `cloud-mall/cloud-mall-framework/src/main/resources/` 下的 Mapper XML 中定义的 CREATE TABLE。
需要复制的表：
- `cm_auth_user`（用户认证表）
- `cm_auth_role`（角色表）- 如存在
- `cm_auth_user_role`（用户角色关系表）- 如存在

复制后粘贴到 `docs/sql/cm_auth.sql`。

- [ ] **Step 3: 启动基础设施**

Run:
```bash
docker compose -f docs/docker/docker-compose-infra.yml up -d
```

Expected: Nacos 控制台可通过 `http://localhost:8848/nacos` 访问
默认用户名密码: `nacos/nacos`

- [ ] **Step 4: Commit**

```bash
git add docs/ && git commit -m "feat(infra): add Nacos + MySQL Docker Compose and auth DDL"
```

---

### Task 5: auth-biz — 认证服务实现

**Files:**
- Create: `auth-service/auth-biz/src/main/resources/application.yml`
- Create: `auth-service/auth-biz/src/main/resources/bootstrap.yml`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/AuthApplication.java`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/entity/UserDO.java`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/mapper/UserMapper.java`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/service/IAuthService.java`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/service/impl/AuthServiceImpl.java`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/controller/AuthController.java`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/config/SecurityConfig.java`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/security/JwtTokenProvider.java`
- Create: `auth-service/auth-biz/src/main/java/com/cloudmall/auth/security/filter/AuthTokenAuthenticationFilter.java`

**Interfaces:**
- Consumes: `LoginRequest`, `LoginResponse`, `UserInfoResponse`, `RegisterRequest`, `AuthClient`, `Result`, `AuthUserContext`, `BizException`, `BizErrorCode`
- Produces: auth-service 在 Nacos 注册，端口 8081，`POST /auth/login` 可调用

- [ ] **Step 1: 创建配置文件和启动类**

```yaml
# bootstrap.yml — 从 Nacos 读取配置
spring:
  application:
    name: auth-service
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
```

```yaml
# application.yml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cm_auth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.cloudmall.auth.entity
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

spring.cloud.nacos.discovery.server-addr: localhost:8848
```

```java
// AuthApplication.java
package com.cloudmall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
```

- [ ] **Step 2: 从原项目迁移 auth 相关代码**

源文件路径对照表：

| 原文件（cloud-mall） | 目标文件（cloud-mall-cloud） | 调整内容 |
|---------------------|-----------------------------|---------|
| `framework/.../models/auth/entity/AuthUser.java` | `auth-biz/.../entity/AuthUserDO.java` | 包路径改为 `com.cloudmall.auth.entity`；类名加 DO 后缀 |
| `framework/.../models/auth/mapper/AuthUserMapper.java` | `auth-biz/.../mapper/AuthUserMapper.java` | 包路径改为 `com.cloudmall.auth.mapper` |
| `framework/.../models/auth/service/IAuthService.java` | `auth-biz/.../service/IAuthService.java` | 只保留 login/register/getUserInfo 方法声明 |
| `framework/.../models/auth/service/impl/AuthServiceImpl.java` | `auth-biz/.../service/impl/AuthServiceImpl.java` | 只保留 login/register/getUserInfo 实现；移除引用其他域的代码 |
| `framework/.../security/JwtTokenProvider.java` | `auth-biz/.../security/JwtTokenProvider.java` | 包路径改为 `com.cloudmall.auth.security` |
| `framework/.../security/handler/LoginSuccessHandler.java` | 不迁移 | 网关层处理 token 校验 |
| `framework/.../config/SecurityConfig.java` | 不迁移 | auth-biz 不使用 Spring Security（网关负责校验） |

**关键规则：**
- 所有 `com.cloudmall.framework` -> `com.cloudmall.auth`
- 移除 `BizErrorCode.xxx` 中不属于 auth 的错误码引用（如 `ORDER_NOT_EXIST`）
- 移除 `@Autowired` 依赖的其他服务 bean（如 OrderService）
- 保留 MyBatis-Plus Mapper + 基础 CRUD

- [ ] **Step 3: 实现 AuthController**

```java
package com.cloudmall.auth.controller;

import com.cloudmall.api.auth.request.LoginRequest;
import com.cloudmall.api.auth.response.LoginResponse;
import com.cloudmall.api.auth.response.UserInfoResponse;
import com.cloudmall.common.entity.Result;
import com.cloudmall.auth.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @GetMapping("/userinfo")
    public Result<UserInfoResponse> getUserInfo(@RequestParam Long userId) {
        return Result.success(authService.getUserInfo(userId));
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `mvn clean compile -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 5: 启动 auth-service 并验证 Nacos 注册**

Run:
```bash
# 确保 Nacos 已在运行
docker compose -f docs/docker/docker-compose-infra.yml ps
# 启动 auth-service
mvn -pl auth-service/auth-biz spring-boot:run
```

验证：
1. Nacos 控制台 `http://localhost:8848/nacos` → 服务列表 → 可见 `auth-service`
2. `curl -X POST http://localhost:8081/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123","userType":"manager"}'` → 返回 token

- [ ] **Step 6: Commit**

```bash
git add auth-service/auth-biz/ && git commit -m "feat(auth): implement auth-service with login/register"
```

---

### Task 6: Gateway 服务

**Files:**
- Create: `gateway/src/main/java/com/cloudmall/gateway/GatewayApplication.java`
- Create: `gateway/src/main/java/com/cloudmall/gateway/config/GatewayConfig.java`
- Create: `gateway/src/main/java/com/cloudmall/gateway/filter/AuthGlobalFilter.java`
- Create: `gateway/src/main/resources/application.yml`

**Interfaces:**
- Consumes: auth-service (通过 lb://auth-service 路由)
- Produces: Gateway 端口 8080，路由 `/auth/**` → auth-service

- [ ] **Step 1: 创建 Gateway pom.xml**

```xml
<!-- gateway/pom.xml 关键依赖 -->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cloudmall</groupId>
        <artifactId>common</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cloudmall</groupId>
        <artifactId>api</artifactId>
    </dependency>
</dependencies>
```

**注意：** Gateway 依赖 `spring-cloud-starter-gateway`，不要引入 `spring-boot-starter-web`（两者冲突）

- [ ] **Step 2: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=0

      # 全局跨域配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
```

- [ ] **Step 3: 创建 Gateway 启动类**

```java
package com.cloudmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建 Auth 全局过滤器**

```java
package com.cloudmall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    // 白名单路径（不需要 token）
    private static final List<String> WHITE_LIST = List.of(
        "/auth/login", "/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单放行
        if (WHITE_LIST.stream().anyMatch(path::endsWith)) {
            return chain.filter(exchange);
        }

        // 从 Header 获取 token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 解析 JWT -> 放入 Header 传递给下游
        // 暂简单实现：透传 Token，auth-service 负责校验
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
```

- [ ] **Step 5: 验证完整链路**

Run:
```bash
# 1. 启动 Nacos
docker compose -f docs/docker/docker-compose-infra.yml up -d
# 2. 启动 auth-service（另一个终端）
mvn -pl auth-service/auth-biz spring-boot:run -Dspring-boot.run.profiles=local
# 3. 启动 gateway（另一个终端）
mvn -pl gateway spring-boot:run -Dspring-boot.run.profiles=local
```

验证：
```bash
# 通过 Gateway 登录（而不是直连 auth-service）
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","userType":"manager"}'
```

Expected: 返回 token（与直连 auth-service:8081 结果一致）

- [ ] **Step 6: Commit**

```bash
git add gateway/ && git commit -m "feat(gateway): add Spring Cloud Gateway with auth routing"
```

---

## 计划验证标准

完成以上 6 个 Task 后：

1. ✅ `mvn clean compile -DskipTests` 编译通过
2. ✅ Nacos 控制台可见 `auth-service` 和 `gateway` 两个服务
3. ✅ `curl localhost:8080/auth/login` 通过 Gateway 登录成功
4. ✅ `curl localhost:8080/auth/userinfo?userId=1` 通过 Gateway 获取用户信息
