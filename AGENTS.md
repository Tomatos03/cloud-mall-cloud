# Repository Guidelines

`cloud-mall-cloud` 是基于 **Spring Boot 3.5.7 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0** 的电商微服务平台，Java 17，Maven 多模块构建。

## 技术栈

- 注册中心/配置中心 Nacos v3.0.3；网关 Spring Cloud Gateway；数据库 MySQL 8.4 + MyBatis-Plus 3.5.14；缓存 Redis 7；搜索引擎 Elasticsearch 8.15.5；消息队列 RocketMQ；AI Spring AI 1.1.8；DTO 转换 MapStruct 1.6.3；工具库 Hutool 5.8.42 + Lombok；反向代理 Nginx。

## 模块结构

- 顶层：`common/`（共享工具、通用枚举与跨服务对象）、`gateway/`、`service-api/`（Feign 接口 + DTO 聚合，父 POM）、`services/`（业务服务，父 POM）、`starters/`（自研 Starter，父 POM）、`docs/`（设计/compose/Nacos 配置/SQL）。
- 业务服务（`services/`）：`user-service`（UserApplication，认证/RBAC + 用户资料 + 商家入驻）、`shared-service`（SharedServiceApplication，文件上传/访问 + 文件台账回收）、`product-service`（ProductApplication）、`order-service`（OrderApplication）、`coupon-service`（CouponApplication）、`seckill-service`（SeckillApplication）、`im-service`（IMApplication）、`aigc-service`（agent-service，AIGCApplication）、`mock-payment-service`（MockPaymentApplication，微信支付 API v3 模拟渠道）。
- 服务接口（`service-api/`）：`shared-api`、`user-api`、`product-api`、`order-api`、`coupon-api`、`seckill-api`、`search-api`、`payment-api`、`aigc-api`、`im-api`。只放 req/resp/event/dto/ws 与契约枚举，不放 DO/BO。
- 自研 Starter（`starters/`）：`cloudmall-common`（Result/BizException/BizErrorEnum/UserContextFilter）、`cloudmall-mybatis-plus-annotation`（`@QueryCondition`、`OperatorEnum`，零依赖契约注解）、`cloudmall-mybatis-plus`（MyMetaObjectHandler、`LambdaWrappers`、`Pages`、`PageResponses`、`SqlLimit`）、`cloudmall-jwt`、`cloudmall-rocketmq`（MQProducer/TxMessageHeader）、`cloudmall-outbox`（本地消息 Outbox）、`cloudmall-feign`（用户上下文透传 + Result 自动拆箱）、`cloudmall-elasticsearch`、`cloudmall-xxl-job`。
- 业务服务分层：`controller`、`service`/`impl`、`mapper`、`model/entity`、`model/bo`、`convert`（MapStruct）、`query`、`application`、`job`。Req/Resp 已迁至对应 `service-api` 模块。
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
- 特殊情况：并发“先查再插”用数据库唯一键兜底时，若表缺少对应唯一约束，应先补 DDL 再在代码中捕获 `DuplicateKeyException` 回查，禁止仅靠应用层的 `Asserts.isNull` 判重。当前已知待收敛的约束见 `problem/concurrency-issues.md`。

## 模拟与占位实现

- 以下实现是设计上的模拟/降级，不是待办 Bug，修改时请勿误判为遗漏：
  - `mock-payment-service`：模拟微信支付 API v3 渠道；回调加解密使用仓库内测试密钥。
  - `OrderEventService.convertThirdPartyAmount()`：真实渠道金额单位换算的占位，原样返回入参。
  - `Search IndexInitializer`：ES 不可用时仅记录日志并定时重试，不阻断启动。
  - 本地联调的 `cloudmall.merchant.auto-review-delay-seconds` 仅在 Nacos 本地配置中大于 0；生产必须为 0 并走人工审核。

## API 规范

- RESTful Level 2：资源名复数（`/users`），操作由 HTTP 方法表达（GET 查 / POST 建 / PUT 改 / DELETE 删），认证接口用动词（`/auth/login`）。
- 统一返回 `Result<T>`，业务异常使用 `BizException` + `BizErrorEnum`。

## 架构要点

- Nacos (localhost:8848) 注册发现与配置管理；JWT 无状态认证（userId/username/userType）；Spring Security 鉴权在网关层；Feign 拦截器自动透传用户上下文。

## 对象模型与目录边界

对象按职责分层，**目录与命名必须遵循下表**：

| 目录 | 对象 | 后缀 | 职责 | 允许出现的位置 |
|---|---|---|---|---|
| `services/*/model/entity/` | 持久化对象 | `XxxDO` | 与数据库表一一对应，字段即列 | Mapper、Service 内部 |
| `services/*/model/bo/` | 业务对象 | 领域名（不加 `BO` 后缀） | 多表聚合、跨领域组合、复杂计算结果 | Service、Application 内部 |
| `service-api/*/model/req/` | HTTP 入参 | `XxxReq` | HTTP/Feign 契约入参 | api 模块、Controller、Service |
| `service-api/*/model/resp/` | HTTP 出参 | `XxxResp` | HTTP/Feign 契约出参 | api 模块、Controller、Service |
| `service-api/*/model/event/` | MQ 事件 | `*Event` | 跨服务消息体 | 生产者、消费者 |
| `service-api/*/model/dto/` | 契约内嵌结构 | 领域名 | 请求/响应中的嵌套元素 | api 模块 |
| `service-api/*/model/ws/` | WebSocket 报文 | `XxxReq` | WebSocket 入站报文 | api 模块、WebSocket Handler |
| `common/model/` | 共享契约 | `PageReq`/`PageResp`/`Result` 等 | 跨服务通用分页与结果对象 | api 模块、service |

边界规则：

- **DO 不出 Service 层**：Controller 签名、Feign 接口、`service-api` 中禁止出现 `XxxDO`。
- **Req/Resp 一律在 `service-api`**：service 的 `model/req`、`model/resp` 已取消；Req 为纯 DTO，可继承 `common.model.PageReq`，不得引用 DO 或 MP 实现类型。
- **契约 DTO 只含框架注解元数据**：`@QueryCondition`/`OperatorEnum` 来自 zero-dependency 的 annotation 模块；出参分页用 `common.model.PageResp`，Controller 不返回 `IPage`。
- **BO 只在 Service/Application 内部流转**：禁止作为 Controller 入参/出参，禁止被 `service-api` 引用。
- **转换统一走 `convert/XxxConverter`**（MapStruct）：DO ↔ BO ↔ Req/Resp，禁止在 Controller / Service 内联手写转换。
- **没有聚合需求不建 BO**：BO 用于多表聚合或跨领域组合，不要为每张表机械地造一个 BO。
- **BO 使用领域名，不强制 `BO` 后缀**：如 `CheckoutContext`、`ProductSkuPrice`；仅在与其它层对象重名时加限定词区分。

## 基础设施分层

- 技术能力适配统一放 `infrastructure/<能力>/`，目录名用能力名而非技术栈名（`store`、`parser`、`cache`、`ratelimit`、`push`、`websocket`、`search`、`storage`、`support`、`calc`）；跨服务同类能力必须同名，分批迁移期间未落地的目录作为规划保留。
- 每个能力目录只允许接口 + `impl/` 实现；仅该能力内部使用的枚举与序列化结构随能力放置，共享领域/契约枚举放 `enums/`，契约 DTO 放 `service-api/model/dto/`。
- 配置、配置属性、静态常量分别放 `config/`、`properties/`、`constant/`（接口形式）；业务策略类放 `application`/`service`；MQ consumer、tx-listener、job、controller、filter、interceptor 等入口适配器不放 infrastructure。
- 特殊情况：`coupon-service` 的 `calc/`（优惠计算与范围匹配）是领域计算的显式例外，因算法独立、可单测、与业务流程解耦而归入 infrastructure；其他服务的业务策略仍按上一条放 `application`/`service`，不得援引此例。
- 当前已落地：`aigc-service` 的 `store`/`parser`；`coupon-service` 的 `cache`/`ratelimit`/`calc`；`seckill-service` 的 `cache`/`ratelimit`；`order-service` 的 `cache`；`shared-service` 的 `storage`；`search-service` 的 `search`；`im-service` 的 `store`/`push`/`websocket`/`support`。未使用的目录作为后续扩展规划保留。

## 错误码分区

- 1xxx 通用、10xxx 认证、20xxx 商品、30xxx 地址、40xxx 搜索、45xxx 轮播图、50xxx 订单、60xxx 购物车、70xxx 评论、80xxx 文件、85xxx 资源、90xxx 审核、91xxx AI Agent、92xxx 秒杀、93xxx 优惠券、94xxx IM、95xxx 支付、96xxx 商家入驻、500xx 权限与店铺。

## Coding Style & Naming Conventions

### 命名

- 业务接口一律使用大写 `I` 前缀：Service `I*Service`、Application `I*AppService`、Handler `I*Handler`、Step `I*Step`、Publisher `I*Publisher`、Parser `I*Parser`、Agent `I*Agent` 等，实现类去掉 `I` 前缀；Mapper / Converter / Feign Client 遵循框架约定，不加 `I`；分页查询方法命名 `page*`，Controller 方法名与 Service 保持一致。
- 请求对象以 `XxxReq` 结尾，独立接口响应对象以 `XxxResp` 结尾；响应内部嵌套结构（不单独对外返回，如 `ProductDetailResp.skus` 的元素）不需要 `Resp` 后缀，直接以领域名命名（如 `ProductSku`）。
- 枚举类型统一以 `Enum` 结尾（如 `OrderStatusEnum`），枚举常量使用大写下划线（如 `ORDER_PAID`）。
- 配置类与配置属性分离：`config/` 放 `@Configuration` / `@Bean` / `@AutoConfiguration`；`properties/` 放 `@ConfigurationProperties` 绑定类。

### 分层职责

- Controller 只接收参数、调用 Service，禁止业务逻辑；Service 负责业务规则、数据一致性、事务管理。

### Controller

- 方法参数 ≥ 4 个时封装为 `xxxReq` 对象，禁止逐一声明 `@RequestParam`。

### 内部接口与 FeignClient 一致性

- 内部端点基路径 `/internal/{服务名}`（模块短名），禁止业务路径（如 `/internal/order-events`）；Controller 类名 `Internal{服务名}Controller`，客户端类名 `Internal{服务名}Client`（`contextId` 为 `internal{服务名}Client`）；同一服务内部接口合并单文件，FeignClient `path` 与 Controller 基路径一致；端点变更必须同步 FeignClient。

### 返回与异常

- 统一 `Result<T>`（`Result.success()` / `Result.error()`）；业务异常用 `BizException` + `BizErrorEnum`，禁止裸 `Exception` / `RuntimeException`。

### 条件校验

- 使用 `Asserts` 断言（`notNull` / `isNull` / `isTrue` / `isFalse` / `notEmpty`），禁止手写 `if (x) { throw ... }`；缺方法先扩展 `Asserts`。
- 把校验封装为私有方法时，按返回值区分命名：有返回值（返回校验通过的对象/值）用 `requireXxx`（如 `requireActivityProduct`），无返回值（仅断言校验）用 `checkXxx`（如 `checkUserRateLimit`）。

### 集合判空

- 使用 Hutool `CollectionUtil.isEmpty()` / `isNotEmpty()`，禁止手写 `== null || .isEmpty()`。

### 集合转换与过滤

- 集合元素映射、建索引、分组统一使用 Hutool `CollStreamUtil`，禁止手写 `stream().map(...).collect(Collectors.toXxx())` 模板：
  - 映射为 List / Set：`CollStreamUtil.toList(collection, mapper)` / `toSet(collection, mapper)`。
  - 按 key 建索引：`CollStreamUtil.toIdentityMap(collection, keyMapper)`（value 为元素本身）或 `toMap(collection, keyMapper, valueMapper)`。
  - 分组：`CollStreamUtil.groupByKey` / `groupKeyValue` / `groupBy`。
- 单步的排序、截取、去重等集合操作优先使用 Hutool `CollUtil` / `ListUtil` 静态工具，禁止为此手写 Stream 模板：
  - 排序：`CollUtil.sort(collection, comparator)` 返回新 `List`；仅在明确允许修改原集合时使用 `ListUtil.sort(list, comparator)`；按属性或拼音排序用 `CollUtil.sortByProperty` / `CollUtil.sortByPinyin`。
  - 截取 / 分页 / 切块：Hutool 没有 `limit` 方法，`limit` 与 `skip` 用 `CollUtil.sub(collection, start, end)`；分页用 `ListUtil.page(pageNo, pageSize, list)`；切块用 `CollUtil.split(collection, size)`（新集合）或 `ListUtil.partition(list, size)`（`subList` 视图，原集合变更会同步）。
  - 去重：`CollUtil.distinct(collection)` 保序返回新 `ArrayList`；按字段去重使用 `CollUtil.distinct(collection, keyGenerator, override)`。
  - 最值与匹配：元素实现 `Comparable` 时用 `CollUtil.max` / `CollUtil.min`；自定义 `Comparator` 的最值仍用 JDK `Collections.min/max`；存在性判断与计数用 `CollUtil.findOne` / `anyMatch` / `allMatch` / `count`。
  - 反转：`ListUtil.reverse(list)` 原地修改，`ListUtil.reverseNew(list)` 返回新列表。
  - 拼接：用 `CollUtil.join(collection, delimiter)` 或带前缀/后缀的重载，替代 `Collectors.joining`。
- 多个中间操作混排（如同时包含 `filter` / `sorted` / `distinct` / `limit`）时，仍使用 JDK Stream 管道并以 `toList()` 收尾，不要强行拆成多步临时集合。
- 过滤后立即转换时，用 `CollUtil.filterNew(collection, predicate)` 组合 `CollStreamUtil`，禁止保留仅用于中转的局部集合，禁止为单行工具调用再封装私有方法。
- 过滤在映射之前时优先 `CollUtil.filterNew` 后再转换，避免对 null 元素执行 mapper；映射函数本身能安全处理 null 时可直接使用 `CollStreamUtil.toList`，其会过滤映射结果为 null 的元素。
- `CollUtil.filter` / `IterUtil.filter` 会原地修改集合，禁止用于 Spring 注入集合或只读集合；需要新集合时使用 `CollUtil.filterNew` 或 `IterUtil.filterToList`。
- 纯过滤且无后续转换时，优先 JDK `stream().filter(...).toList()`，不要为单行工具调用再封装私有方法。
- Hutool 与 JDK Stream 的返回值差异需注意：`CollUtil.distinct` 返回可变 `ArrayList`（`stream().distinct().toList()` 为不可变）；`ListUtil.page` 首页可能返回原列表的不可修改视图；`ListUtil.partition` 返回视图。
- 以下场景保留原写法，避免语义变化：`LinkedHashMap` 顺序分组、带 merge 函数的 `toMap`、依赖重复 key 抛异常的 `toMap`、非 `Collection` 的 `Cursor` 转换。

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
- 分页请求继承 `common.model.PageReq`（纯 DTO，带 1/10 默认值），筛选字段用 `@QueryCondition` 声明；Mapper 用 `LambdaWrappers.of(req, XxxDO.class)` 构建条件、`Pages.from(req)` 构建 MP `Page`；分页出参用 `PageResponses.of(page, converter)` 转成 `PageResp`。
- `LIMIT` 一律用 `SqlLimit.of(n)`，禁止手写 `.last("LIMIT ...")`。
- 含变量拼接的 `inSql` / `setSql` 先抽为紧邻使用处的 `final` 局部变量；动态值优先 `set(实体::getXxx, value)`。
- Mapper 禁止注解脚本 SQL（`@Select` / `@Insert` / `@Update` / `@Delete` 及 Provider 系列），一律 `default` 方法 + Wrapper。
- 特殊情况：Wrapper 无法表达的原子 upsert（如 `INSERT ... ON DUPLICATE KEY UPDATE`）允许使用 XML Mapper，但必须经过评审并在方法 Javadoc 中说明原因；当前唯一授权示例为 `ShopStatMapper.upsertFollowCount`（`services/user-service/src/main/resources/mapper/ShopStatMapper.xml`）。其余 SQL 一律不得新增 XML Mapper。

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
