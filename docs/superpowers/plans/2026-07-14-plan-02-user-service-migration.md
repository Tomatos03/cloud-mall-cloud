# Plan 2: User Service 迁移

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将单体中的用户管理、地址、店铺、RBAC 体系迁移为独立的 user-service，通过 Gateway 对外暴露。

**Architecture:** user-service 独立端口 8082，独立数据库 cm_user，通过 Feign 调用 auth-service 做身份校验，不直接依赖其他业务服务。

**Tech Stack:** Spring Boot 3.5.7, Spring Cloud Alibaba 2025.0.0.0, MyBatis-Plus, MySQL, Nacos

## Global Constraints

- Java 17, Maven, Spring Boot 3.5.7 parent
- Spring Cloud 2025.0.0 + Alibaba 2025.0.0.0
- Request/Response/Client 命名约定
- MyBatis-Plus 3.5.14, Hutool 5.8.42
- 所有 `com.cloudmall.framework` → `com.cloudmall.user`
- No wildcard imports, 4 space indent

---
### Task 1: user-service 模块脚手架

**Files:**
- Create: `user-service/pom.xml` (aggregator)
- Create: `user-service/user-api/pom.xml`
- Create: `user-service/user-biz/pom.xml`
- Modify: `pom.xml` — add `<module>user-service</module>`, add `user-api` to `<dependencyManagement>`

**Interfaces:**
- Consumes: parent POM (common, api)
- Produces: user-service module scaffold, compilable

**user-service/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <artifactId>user-service</artifactId>
    <packaging>pom</packaging>
    <modules>
        <module>user-api</module>
        <module>user-biz</module>
    </modules>
</project>
```

**user-service/user-api/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <artifactId>user-api</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.cloudmall</groupId>
            <artifactId>api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

**user-service/user-biz/pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cloudmall</groupId>
        <artifactId>cloud-mall-cloud</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <artifactId>user-biz</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.cloudmall</groupId>
            <artifactId>user-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.cloudmall</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus-version}</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Root POM updates:**
1. Add `<module>user-service</module>` to parent `<modules>`
2. Add to `<dependencyManagement>`:
```xml
<dependency>
    <groupId>com.cloudmall</groupId>
    <artifactId>user-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

- [ ] **Create POM files and directories**
```bash
mkdir -p user-service/user-api/src/main/java/com/cloudmall/user/api/{request,response,client}
mkdir -p user-service/user-biz/src/main/java/com/cloudmall/user/{controller,service/impl,entity,mapper}
mkdir -p user-service/user-biz/src/main/resources
```
- [ ] `mvn clean compile -DskipTests` → BUILD SUCCESS
- [ ] `git add user-service/ pom.xml && git commit -m "feat(user): add user-service module scaffold"`

---

### Task 2: user-api 合同

**Files:**
- Create: `user-service/user-api/src/main/java/com/cloudmall/user/api/request/AddressCreateRequest.java`
- Create: `user-service/user-api/src/main/java/com/cloudmall/user/api/request/AddressUpdateRequest.java`
- Create: `user-service/user-api/src/main/java/com/cloudmall/user/api/request/UserUpdateRequest.java`
- Create: `user-service/user-api/src/main/java/com/cloudmall/user/api/response/UserResponse.java`
- Create: `user-service/user-api/src/main/java/com/cloudmall/user/api/response/AddressResponse.java`
- Create: `user-service/user-api/src/main/java/com/cloudmall/user/api/response/StoreResponse.java`
- Create: `user-service/user-api/src/main/java/com/cloudmall/user/api/client/UserClient.java`

**Interfaces:**
- Produces: UserClient (Feign), Request/Response classes

**AddressCreateRequest.java:**
```java
package com.cloudmall.user.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressCreateRequest {
    @NotBlank
    private String consignee;
    @NotBlank
    private String phone;
    @NotBlank
    private String province;
    @NotBlank
    private String city;
    @NotBlank
    private String district;
    @NotBlank
    private String detail;
    private String zipCode;
    private Boolean isDefault;
}
```

**AddressUpdateRequest.java:** same fields + `private Long id;`

**UserUpdateRequest.java:**
```java
package com.cloudmall.user.api.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private Long id;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
}
```

**UserResponse.java:**
```java
package com.cloudmall.user.api.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String userType;
    private Integer status;
}
```

**AddressResponse.java:**
```java
package com.cloudmall.user.api.response;

import lombok.Data;

@Data
public class AddressResponse {
    private Long id;
    private Long userId;
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String zipCode;
    private Boolean isDefault;
}
```

**StoreResponse.java:**
```java
package com.cloudmall.user.api.response;

import lombok.Data;

@Data
public class StoreResponse {
    private Long id;
    private Long userId;
    private String storeName;
    private String storeLogo;
    private String description;
    private Integer status;
}
```

**UserClient.java:**
```java
package com.cloudmall.user.api.client;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.response.AddressResponse;
import com.cloudmall.user.api.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", path = "/user")
public interface UserClient {

    @GetMapping("/{id}")
    Result<UserResponse> getUserById(@PathVariable("id") Long id);

    @GetMapping("/address/list")
    Result<List<AddressResponse>> listAddresses(@RequestParam("userId") Long userId);
}
```

- [ ] Create all 7 files
- [ ] `mvn clean compile -DskipTests` → BUILD SUCCESS
- [ ] `git add user-service/user-api/ && git commit -m "feat(user-api): add user api contracts"`

---

### Task 3: User DDL + 配置

**Files:**
- Create: `docs/sql/cm_user.sql`
- Create: `user-service/user-biz/src/main/resources/application.yml`

**cm_user.sql:**
```sql
CREATE DATABASE IF NOT EXISTS cm_user DEFAULT CHARACTER SET utf8mb4;
USE cm_user;

-- Address (从原项目 address 表复制)
CREATE TABLE `cm_address` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `consignee` varchar(100) NOT NULL COMMENT '收货人',
    `phone` varchar(20) NOT NULL COMMENT '电话',
    `province` varchar(50) DEFAULT NULL,
    `city` varchar(50) DEFAULT NULL,
    `district` varchar(50) DEFAULT NULL,
    `detail` varchar(255) DEFAULT NULL,
    `zip_code` varchar(10) DEFAULT NULL,
    `is_default` tinyint(1) DEFAULT '0',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Store (从原项目 store 表复制)
CREATE TABLE `cm_store` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '商家用户ID',
    `store_name` varchar(200) NOT NULL,
    `store_logo` varchar(500) DEFAULT NULL,
    `description` text,
    `status` tinyint(1) DEFAULT '1',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `deleted` tinyint(1) DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**application.yml:**
```yaml
server:
  port: 8082

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:mysql://localhost:3306/cm_user?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.cloudmall.user.entity
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

spring.cloud.nacos.discovery.server-addr: localhost:8848
```

- [ ] Create both files
- [ ] `git add docs/sql/cm_user.sql user-service/user-biz/src/main/resources/ && git commit -m "feat(user): add user DDL and config"`

---

### Task 4: user-biz 实现

**Files:**
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/UserApplication.java`
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/entity/AddressDO.java`
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/entity/StoreDO.java`
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/mapper/AddressMapper.java`
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/mapper/StoreMapper.java`
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/service/IAddressService.java`
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/service/impl/AddressServiceImpl.java`
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/controller/AddressController.java`
- Create: `user-service/user-biz/src/main/java/com/cloudmall/user/controller/StoreController.java`

**UserApplication.java:**
```java
package com.cloudmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
```

**AddressDO.java:**
```java
package com.cloudmall.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cm_address")
public class AddressDO {
    private Long id;
    private Long userId;
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String zipCode;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
```

**StoreDO.java:** mirror of cm_store columns with @TableName("cm_store")

**AddressMapper.java:**
```java
package com.cloudmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudmall.user.entity.AddressDO;

public interface AddressMapper extends BaseMapper<AddressDO> {}
```

**StoreMapper.java:** extends BaseMapper<StoreDO>

**IAddressService.java:**
```java
package com.cloudmall.user.service;

import com.cloudmall.user.api.request.AddressCreateRequest;
import com.cloudmall.user.api.request.AddressUpdateRequest;
import com.cloudmall.user.api.response.AddressResponse;
import java.util.List;

public interface IAddressService {
    List<AddressResponse> listByUserId(Long userId);
    AddressResponse getById(Long id);
    Long create(AddressCreateRequest request, Long userId);
    void update(AddressUpdateRequest request);
    void delete(Long id);
}
```

**AddressServiceImpl.java:**
```java
package com.cloudmall.user.service.impl;

import com.cloudmall.common.exception.BizException;
import com.cloudmall.common.enums.BizErrorCode;
import com.cloudmall.user.api.request.AddressCreateRequest;
import com.cloudmall.user.api.request.AddressUpdateRequest;
import com.cloudmall.user.api.response.AddressResponse;
import com.cloudmall.user.entity.AddressDO;
import com.cloudmall.user.mapper.AddressMapper;
import com.cloudmall.user.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressResponse> listByUserId(Long userId) {
        List<AddressDO> list = addressMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AddressDO>()
                .eq(AddressDO::getUserId, userId)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public AddressResponse getById(Long id) {
        AddressDO addr = addressMapper.selectById(id);
        if (addr == null) throw new BizException(BizErrorCode.DATA_NOT_FOUND);
        return toResponse(addr);
    }

    @Override
    public Long create(AddressCreateRequest request, Long userId) {
        AddressDO addr = new AddressDO();
        addr.setUserId(userId);
        addr.setConsignee(request.getConsignee());
        addr.setPhone(request.getPhone());
        addr.setProvince(request.getProvince());
        addr.setCity(request.getCity());
        addr.setDistrict(request.getDistrict());
        addr.setDetail(request.getDetail());
        addr.setZipCode(request.getZipCode());
        addr.setIsDefault(request.getIsDefault() != null && request.getIsDefault());
        addressMapper.insert(addr);
        return addr.getId();
    }

    @Override
    public void update(AddressUpdateRequest request) { /* 同 create 逻辑 */ }

    @Override
    public void delete(Long id) { addressMapper.deleteById(id); }

    private AddressResponse toResponse(AddressDO addr) {
        AddressResponse r = new AddressResponse();
        r.setId(addr.getId());
        r.setUserId(addr.getUserId());
        r.setConsignee(addr.getConsignee());
        r.setPhone(addr.getPhone());
        r.setProvince(addr.getProvince());
        r.setCity(addr.getCity());
        r.setDistrict(addr.getDistrict());
        r.setDetail(addr.getDetail());
        r.setZipCode(addr.getZipCode());
        r.setIsDefault(addr.getIsDefault());
        return r;
    }
}
```

**AddressController.java:**
```java
package com.cloudmall.user.controller;

import com.cloudmall.common.entity.Result;
import com.cloudmall.user.api.request.AddressCreateRequest;
import com.cloudmall.user.api.request.AddressUpdateRequest;
import com.cloudmall.user.api.response.AddressResponse;
import com.cloudmall.user.service.IAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/address")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @GetMapping("/list")
    public Result<List<AddressResponse>> list(@RequestParam Long userId) {
        return Result.success(addressService.listByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<AddressResponse> getById(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid AddressCreateRequest request,
                                @RequestHeader("X-User-Id") Long userId) {
        return Result.success(addressService.create(request, userId));
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid AddressUpdateRequest request) {
        addressService.update(request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success(null);
    }
}
```

**StoreController.java:** basic CRUD on stores, GET by userId, GET by id.

- [ ] Create all files
- [ ] `mvn clean compile -DskipTests` → BUILD SUCCESS
- [ ] `git add user-service/user-biz/ && git commit -m "feat(user-biz): implement user-service with address/store"`

---

### Task 5: Gateway 路由 + 验证

**Files:**
- Modify: `gateway/src/main/resources/application.yml` — add user-service route

**Gateway route addition:**
```yaml
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/user/**
          filters:
            - StripPrefix=0
```

Add to whitelist in `AuthGlobalFilter.java` if needed (e.g., `/user/store/public` or similar public endpoints).

- [ ] Update gateway route config
- [ ] `mvn clean compile -DskipTests` → BUILD SUCCESS
- [ ] `git add gateway/ && git commit -m "feat(gateway): add user-service route"`
