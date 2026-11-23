# cloudmall-outbox-spring-boot-starter

通用本地消息 Outbox。生产侧在业务事务内写入事件，消费侧按事件类型扫描并处理。

- 表结构固定为 `cm_message_outbox`，不做表名/列名映射；
- 依赖 MyBatis-Plus（项目自研 starter）与 `spring-boot-starter-aop`，不依赖 RocketMQ、xxl-job；
- 投递/处理逻辑由各服务自行实现，starter 只提供写入与通用消费操作。

## 表结构

```sql
CREATE TABLE cm_message_outbox (
    id              bigint      NOT NULL AUTO_INCREMENT,
    app_name        varchar(50) NOT NULL COMMENT '来源服务',
    event_type      varchar(50) NOT NULL COMMENT '事件类型，建议带域前缀',
    biz_id          varchar(64) DEFAULT NULL COMMENT '业务ID',
    payload         text COMMENT '事件载荷：字符串原样写入，对象序列化为 JSON',
    status          varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/DONE/FAILED',
    retry_count     int         NOT NULL DEFAULT 0,
    next_retry_time datetime    DEFAULT NULL,
    create_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_status (status, next_retry_time),
    KEY idx_type_status (event_type, status),
    KEY idx_app_status (app_name, status),
    KEY idx_biz (biz_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
```

`app_name` 自动取自 `spring.application.name`。

## 写入

### 单事件

```java
@OutboxEvent(eventType = "FILE_BIND", bizId = "#userId", payload = "#url")
@Transactional
public void bindAvatar(Long userId, String url) {
    userMapper.updateAvatar(userId, url);
}
```

`payload` 是 SpEL：传字符串原样写入，传对象序列化为 JSON。`condition` 可用于按需跳过：

```java
@OutboxEvent(
        eventType = "FILE_BIND",
        bizId = "#message.senderId",
        payload = "#fileUrl",
        condition = "#fileUrl != null"
)
```

### 多事件

注解留空 `eventType`，方法内收集：

```java
@OutboxEvent(required = false)
@Transactional
public void updateProfile(Long userId, UpdateProfileReq req) {
    ...
    OutboxEvents.collect("FILE_BIND", String.valueOf(userId), req.getAvatar());
    OutboxEvents.collect("FILE_RELEASE", String.valueOf(userId), previousAvatar);
}
```

同一事件类型有多条载荷时用批量方法：

```java
OutboxEvents.collectAll("FILE_BIND", String.valueOf(userId), photoUrls);
```

- `required = false`：允许本次调用不产生事件；默认 `true`，未产生事件时抛错回滚。
- 方法必须经 Spring 代理调用，`this.xxx()` 自调用不会触发切面。
- 同一调用链中不要嵌套使用 `@OutboxEvent`。

## 消费

```java
@XxlJob("fileEventApply")
public void apply() {
    outboxRepository.consume(Set.of("FILE_BIND", "FILE_RELEASE"), record -> {
        switch (record.eventType()) {
            case "FILE_BIND" -> fileService.bind(Long.valueOf(record.bizId()), null, List.of(record.payload()));
            case "FILE_RELEASE" -> fileService.release(List.of(record.payload()));
            default -> throw new IllegalStateException("未知事件类型");
        }
    });
}

@XxlJob("fileEventCleanup")
public void cleanup() {
    outboxRepository.cleanup();
}
```

`consume` 的执行流程：

1. 扫描 `PENDING` 且到期的记录；
2. `claim` 抢占 `PENDING → PROCESSING`（多实例安全）；
3. 在独立事务内执行 `handler` 并 `markDone`；
4. 处理失败时递增 `retry_count`，未达上限回到 `PENDING` 并退避，达到上限转为 `FAILED`。

下层原语也可单独使用：`listPending`、`claim`、`markDone`、`markFailed`、`cleanup`。

## 配置

```yaml
cloudmall:
  outbox:
    batch-size: 100        # 单批扫描条数
    max-attempts: 10       # 最大重试次数
    backoff-seconds: 30    # 重试退避秒数
    retention-days: 7      # 已完成事件保留天数
```

全部有默认值，可省略。

## 状态机

```text
PENDING ──claim──> PROCESSING ──成功──> DONE
                        │
                        └──失败──> PENDING（退避）/ FAILED（达到上限）
```

`PROCESSING` 在进程异常退出后会残留，建议按业务需要补充"超时 PROCESSING 重置为 PENDING"的兜底任务。
