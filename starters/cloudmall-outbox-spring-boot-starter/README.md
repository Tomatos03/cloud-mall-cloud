# cloudmall-outbox-spring-boot-starter

通用本地消息 Outbox 自动配置。所有服务共用一张 `cm_message_outbox` 表（`app_name` 区分来源），业务变更与 Outbox 写入在同一本地事务提交，由 xxl-job 定时扫描发布到 RocketMQ。

## 使用方式

业务服务只需两处接入，无需注册事件类型：payload 以原始 JSON 字节发布，事件结构知识由消费端持有。

1. 业务事务内追加事件（`OutboxAppender.append` 生成并注入 eventId，保证行内 event_id 与 payload 内 eventId 一致）：

```java
OrderEvent event = OrderEvent.builder()
                             .orderNo(orderNo)
                             .userId(order.getUserId())
                             .build();
outboxAppender.append(
        OutboxAppendReq.builder()
                       .topic(ORDER_EVENTS_TOPIC)
                       .eventType(OrderTagEnum.PAID.getTag())
                       .aggregateType("ORDER")
                       .aggregateId(orderNo)
                       .event(event)
                       .build()
);
```

2. 注册调度任务：starter 已提供默认任务 Bean（`OutboxJobHandlers`：`outboxPublish` 发布 + `outboxCleanup` 清理），服务只需在 xxl-job 配置中声明触发：

```yaml
xxl:
  job:
    auto-register:
      jobs:
        - handler: outboxPublish
          desc: Outbox事件发布
          schedule-type: FIX_DELAY
          schedule-conf: "5"
        - handler: outboxCleanup
          desc: Outbox已发布记录清理
          schedule-type: CRON
          schedule-conf: "0 0 3 * * ?"
```

`OutboxJobHandlers` 由 `OutboxAutoConfiguration` 条件装配（classpath 存在 xxl-job 与分发器才生效），批次大小与保留天数走 `cloudmall.outbox.batch-size` / `cloudmall.outbox.retention-days` 配置（默认 100 / 7），服务可声明同名 Bean 覆盖默认实现。

`appName` 自动从 `spring.application.name` 解析，无需显式传入。

## 约定与约束

- 事件对象必须包含 `eventId` 字段（消费端幂等键），由 `OutboxAppender` 生成并注入。
- 消费端幂等表 `cm_message_consume_record` 按 `(event_id, consumer, operation_type)` 去重，重复发布无害。
- `OutboxMapper` 通过 `OutboxAutoConfiguration` 的 `@AutoConfigurationPackage(basePackages = "com.cloudmall.outbox")` + `@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)` 保证先于 MyBatis 自动扫描器注册包，再由 `@Mapper` 自动扫描注册；消费方无需额外 `@MapperScan`，也不会因引入本 starter 导致自身 Mapper 的自动扫描退避。
- 不依赖行锁：xxl-job 单次触发只在一个执行器实例执行，同一服务多实例不会重复扫描同一批。

## 可插拔发布器

`OutboxDispatcher` 面向 `OutboxPublisher` 抽象发布事件，默认提供 RocketMQ 实现（`RocketMqOutboxPublisher`，通过 `MQProducer` 同步发送）。

**依赖要求**：本 starter 对 `cloudmall-rocketmq-spring-boot-starter` 仅编译期生效（`<optional>true</optional>`，不传递）。使用默认 RocketMQ 发布器的服务必须**显式声明** rocketmq starter；否则 `RocketMqOutboxPublisher` 不会注册（`@ConditionalOnClass(MQProducer.class)` 拦截），需自行提供 `OutboxPublisher` Bean，否则 `OutboxDispatcher` 装配失败（快速失败，提示缺少发布器）。

如需切换传输（如 Feign 同步调用），注册自定义 `OutboxPublisher` Bean 即可覆盖默认实现，分发器与业务代码零改动：

```java
@Bean
public OutboxPublisher feignOutboxPublisher() {
    return (topic, tag, event) -> { /* Feign 调用逻辑 */ };
}
```

## 批量发送

`OutboxDispatcher.publishPending` 把待发布事件按 `(topic, tag)` 分组，同组一次批量发送
（默认 RocketMQ 实现走 `RocketMQTemplate.syncSend(destination, Collection)`，整批一次网络请求）。

- 分组键 = `(topic, tag)`：满足 RocketMQ 批量发送同 topic 约束，每组一个 destination
- 切批：单批超过 `cloudmall.outbox.batch-max-bytes`（默认 1MB）按 payload 字节贪心切批
- 失败语义：`syncSend` 批量是原子的，整批失败 → 组内全部进入既有重试/退避/死信状态
  （消费端幂等表按 `event_id` 去重，重复发布无害）
- 自定义发布器未实现 `publishBatch` 时，默认逐条回退 `publish`，行为与旧版一致

## 处理能力扩展

当前为分组后逐批顺序发送。后续按需逐级扩展，均为增量改动，不涉及表结构与消费端变更：

- 调度频率 / 扫描条数：调整 xxl-job 调度周期与 `publishPending` 的 batchSize。
- 并行发送：把 `OutboxDispatcher.publishPending` 的组循环改为线程池并行，改动封闭在 dispatcher 内部。
- 分片广播：xxl-job 路由策略切分片广播，`OutboxMapper.listDuePending` 增加 `MOD(id, shardTotal) = shardIndex` 可选过滤，各实例处理自己的分片。
