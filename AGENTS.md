# Repository Guidelines

`cloud-mall-cloud` 是基于 **Spring Boot 3.5.7 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0** 的电商微服务平台，Java 17，Maven 多模块构建。

## 技术栈

- 注册中心/配置中心 Nacos v3.0.3；网关 Spring Cloud Gateway；数据库 MySQL 8.4 + MyBatis-Plus 3.5.14；缓存 Redis 7；搜索引擎 Elasticsearch 8.15.5；消息队列 RocketMQ；AI Spring AI 1.1.8；DTO 转换 MapStruct 1.6.3；工具库 Hutool 5.8.42 + Lombok；反向代理 Nginx。

## 模块结构

- 顶层：`common/`（共享工具与跨服务对象）、`gateway/`、`service-api/`（Feign 接口 + DTO 聚合，父 POM）、`services/`（业务服务，父 POM）、`starters/`（自研 Starter，父 POM）、`docs/`（设计/compose/Nacos 配置/SQL）。
- 业务服务（`services/`）：`user-service`（UserApplication，认证/RBAC + 用户资料）、`product-service`（ProductApplication）、`order-service`（OrderApplication）、`coupon-service`（CouponApplication）、`seckill-service`（SeckillApplication）、`im-service`（IMApplication）、`aigc-service`（agent-service，AIGCApplication）。
- 服务接口（`service-api/`）：`user-api`、`product-api`、`order-api`、`coupon-api`、`seckill-api`、`search-api`。
- 自研 Starter（`starters/`）：`cloudmall-common`（Result/BizException/BizError/UserContextFilter）、`cloudmall-mybatis-plus`（MyMetaObjectHandler、PageReq/ConditionReq、LimitEnum、StatusEnum）、`cloudmall-jwt`、`cloudmall-rocketmq`（MQProducer/TxMessageHeader）、`cloudmall-outbox`（本地消息 Outbox）、`cloudmall-feign`（用户上下文透传 + Result 自动拆箱）、`cloudmall-elasticsearch`、`cloudmall-xxl-job`。
- 业务服务分层：`controller`、`service`/`impl`、`mapper`、`entity`、`model/req` 与 `model/resp`、`convert`（MapStruct）、`enums`、`application`、`job`。
- 服务端口统一在 Nacos 配置中心管理（导入 `common` + `{app-name}` 配置）。

## 构建、测试与开发命令

```bash
docker compose -f docs/docker/compose.yaml up -d          # 启动本地依赖（MySQL/Redis/Nacos/ES/Nginx）
mvn clean compile -DskipTests                             # 全量编译
mvn clean compile -pl services/user-service -am           # 单模块编译（含依赖）
mvn clean package                                         # 打包
mvn -pl services/user-service spring-boot:run             # 运行
mvn test                                                  # 测试
```

- 每次修改文件之后，必须在项目工作空间根目录执行 `jstyle format`。

## 数据库约定

- 单库 `cm_mall`（xxl-job 独立库脚本见 `docs/sql/xxl_job`）；SQL 脚本位于 `docs/sql/cm_mall/`（`01-schema.sql` / `02-seed.sql`）。
- 业务表统一前缀 `cm_`；通用字段 `create_time` / `update_time` / `deleted`；关联表仅 `create_time`；软删除 `deleted = 1`。

## API 规范

- RESTful Level 2：资源名复数（`/users`），操作由 HTTP 方法表达（GET 查 / POST 建 / PUT 改 / DELETE 删），认证接口用动词（`/auth/login`）。
- 统一返回 `Result<T>`，业务异常使用 `BizException` + `BizError`。

## 架构要点

- Nacos (localhost:8848) 注册发现与配置管理；JWT 无状态认证（userId/username/userType）；Spring Security 鉴权在网关层；Feign 拦截器自动透传用户上下文。

## 错误码分区

- 1xxx 通用、10xxx 认证、20xxx 商品、30xxx 地址、40xxx 搜索、45xxx 轮播图、50xxx 订单、60xxx 购物车、70xxx 评论、80xxx 文件、85xxx 资源、90xxx 审核、91xxx AI Agent、92xxx 秒杀、93xxx 优惠券、94xxx IM、500xx 权限与店铺。

## aigc-service 特性

- 应用名 `agent-service`，集成 Spring AI 1.1.8：OpenAI 大模型 + ES 向量存储（`cm-agent-knowledge`）+ RAG、文档解析（Tika）、Redis 聊天记忆、OpenAI TTS；项目内 `org/springframework/ai/` 为 Spring AI 补丁类。

## Coding Style & Naming Conventions

### 命名

- Service 接口 `I*Service`（如 `IAuthService`），实现去掉 `I` 前缀；分页查询方法命名 `page*`，Controller 方法名与 Service 保持一致。
- 请求对象以 `XxxReq` 结尾，独立接口响应对象以 `XxxResp` 结尾；响应内部嵌套结构（不单独对外返回，如 `ProductDetailResp.skus` 的元素）不需要 `Resp` 后缀，直接以领域名命名（如 `ProductSku`）。

### 分层职责

- Controller 只接收参数、调用 Service，禁止业务逻辑；Service 负责业务规则、数据一致性、事务管理。

### Controller

- 方法参数 ≥ 4 个时封装为 `xxxReq` 对象，禁止逐一声明 `@RequestParam`。

### 内部接口与 FeignClient 一致性

- 内部端点基路径 `/internal/{服务名}`（模块短名），禁止业务路径（如 `/internal/order-events`）；Controller 类名 `Internal{服务名}Controller`，客户端类名 `Internal{服务名}Client`（`contextId` 为 `internal{服务名}Client`）；同一服务内部接口合并单文件，FeignClient `path` 与 Controller 基路径一致；端点变更必须同步 FeignClient。

### 返回与异常

- 统一 `Result<T>`（`Result.success()` / `Result.error()`）；业务异常用 `BizException` + `BizError`，禁止裸 `Exception` / `RuntimeException`。

### 条件校验

- 使用 `Asserts` 断言（`notNull` / `isNull` / `isTrue` / `isFalse` / `notEmpty`），禁止手写 `if (x) { throw ... }`；缺方法先扩展 `Asserts`。

### 集合判空

- 使用 Hutool `CollectionUtil.isEmpty()` / `isNotEmpty()`，禁止手写 `== null || .isEmpty()`。

### 实体与 DTO

- 注解固定 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`；统一 builder 构建，禁止 `new` 后逐字段 setter；注解写在字段上方，禁止写在泛型参数旁（如 `List<@Valid Xxx>`）；允许纯谓词方法（`is*` / `has*` / `can*`，只读自身字段与入参、无副作用）。

### 注释

- 文字描述，禁止数字序号与 `=` / `-` 分隔线注释；用 Javadoc、空行或提取私有方法组织代码。

### 查询与转换分离

- 查询（mapper）与转换（converter）分步书写，禁止嵌套同一表达式；批量转换调用 converter 的 `toXxxList`，禁止手写 `stream().map(converter::toXxx)`。
- 实体↔DTO 转换一律下沉 `convert/XxxConverter`（MapStruct `@Mapper(componentModel = "spring")`，方法 `toXxx` / `toXxxList` / `toDO`）；Service / Application 禁止内联 `builder()` / setter / `stream()` 手写转换（即使字段一一对应、即使单条），converter 缺失先补充。

### 循环遍历与 Stream 管道

- `for-each` 头部禁止内联 Stream 管道表达式，先抽取为具名局部变量再遍历。

### JDK 17 语法糖

- 优先使用 JDK 17 提供的最新语法糖，禁止退化为旧式写法：
  - `switch` 一律用箭头语法（switch 语句或 switch 表达式），禁止传统 `case X: ... break;` 冒号写法；多分支返回统一用 switch 表达式 + `yield`，纯动作分支用 switch 语句 + 箭头标签（保留原有日志/返回语义的前提下改写）。
  - 类型判断用 `instanceof` 模式匹配（`if (obj instanceof Xxx x)`），禁止先 `instanceof` 再强转。
  - 不可变集合优先 `List.of` / `Map.of` / `Set.of`；Stream 收尾优先 `toList()`，禁止 `collect(Collectors.toList())`。
  - 数据载体优先 `record`；长字符串优先文本块（`"""`）。
- 改写语法糖时保持行为完全等价（分支、返回、日志顺序不变），不改变原有逻辑。

### 静态常量

- 静态常量统一使用接口形式定义（如 `public interface XxxConstants { String XXX = "..." }`），接口常量默认为 `public static final`，禁止用 `final class` + 私有构造器的常量类。
- 使用处一律通过 `XxxConstants.XXX` 静态引用，禁止实现常量接口来继承常量（如 `implements XxxConstants`）。

### MyBatis-Plus

- 使用 `Wrappers.lambdaQuery()` / `lambdaUpdate()`，禁止 `new LambdaQueryWrapper<>()`；更新用单参数 `update(Wrappers.lambdaUpdate()...)`。
- 对 `status` / `enable` 等字段构造条件必须用业务枚举（`*Enum`），禁止魔数或布尔值。
- 分页请求继承 `PageReq<T>` + `@QueryCondition`，Service 只传 req，条件由 `req.buildLambdaWrapper()` 构建，Service 层不接触 `Wrapper`。
- `LIMIT` 一律用 `LimitEnum`（`LimitEnum.of(n)` 或常量），禁止手写 `.last("LIMIT ...")`。
- 含变量拼接的 `inSql` / `setSql` 先抽为紧邻使用处的 `final` 局部变量；动态值优先 `set(实体::getXxx, value)`。
- Mapper 禁止注解脚本 SQL（`@Select` / `@Insert` / `@Update` / `@Delete` 及 Provider 系列），一律 `default` 方法 + Wrapper。

### 工具方法

- 优先 Hutool 替代三元表达式：`ObjectUtil.defaultIfNull`、`BooleanUtil.isTrue/isFalse`、`StrUtil.isBlank`。
- 按 code 查枚举用 `Arrays.stream().filter().findFirst().orElseThrow()`，未命中抛 `IllegalArgumentException`。

### 多行布尔条件

- 换行续排（行首 `||` / `&&`）的复合条件必须抽取为具名谓词方法，禁止在 `if` / `Asserts` 中裸写；落位：实体字段→实体谓词、Req/DTO 字段→DTO 谓词、跨对象→主实体 + 上下文参数、纯外部校验→Service 私有方法；正面命名、取反在调用处、纯函数无副作用。

## Testing Guidelines

- JUnit 5 + `spring-boot-starter-test`，测试放 `src/test/java`；当前无测试源码，触碰核心 Service/Mapper 逻辑时补充 `XxxTest`；方法命名 `should*` / `when*`。

## Commit & Pull Request Guidelines

- Conventional Commits 中文摘要，带模块 scope：`feat(order): 新增...`、`refactor:`、`fix:`、`docs:`、`style:`、`chore:`。
- PR 说明改动、关联 issue，UI/API 变更附截图。

## Agent-Specific Instructions

- 子目录可添加模块级 `AGENTS.md` 补充指引。

