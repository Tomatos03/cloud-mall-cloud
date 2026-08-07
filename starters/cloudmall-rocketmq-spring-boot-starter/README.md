# cloudmall-rocketmq-spring-boot-starter

RocketMQ 消息基础设施 starter，提供通用发送客户端 `MQProducer` 与消费幂等台账能力，并内置 `AbstractMessageConsumer` 消费模板实现「解析 → tag 分发 → 幂等守卫 → 业务处理 → 记账」的标准流程。

## 功能

### 1. 通用发送客户端（`MQProducer`）

对 `RocketMQTemplate` 的轻量封装，支持常用发送方式：

| 方法 | 说明 |
|------|------|
| `sendSync` | 同步发送（支持超时、tag、消息 key） |
| `sendAsync` | 异步发送（默认 / 自定义回调） |
| `sendOneWay` | 单向发送（不等待 Broker 确认） |
| `sendDelay` / `sendDelaySeconds` | 延迟消息（级别 / 秒） |
| `sendSyncOrderly` | 顺序消息（hashKey 选择队列） |
| `sendInTransaction` | 事务消息（需配置事务监听器） |
| `sendSyncBatch` / `sendBatch` | 批量发送（同一 destination，整批一次请求） |

目标地址格式 `topic:tag`，不指定 tag 时冒号可省略：

```java
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final MQProducer mqProducer;

    public void publishPaid(OrderEvent event) {
        mqProducer.sendSync("ORDER_EVENTS", OrderTagEnum.PAID.getTag(), event);
        // 异步发送 + 默认日志回调
        mqProducer.sendAsync("ORDER_EVENTS:OrderPaid", event, event.getOrderNo());
    }
}
```

异步发送默认回调 `DefaultSendCallback` 自动记录成功 / 失败日志，可继承覆写 `onSuccessExt` / `onExceptionExt` 扩展重试、告警等逻辑。

### 2. 消费幂等台账（`MessageConsumeService`）

基于 `cm_message_consume_record` 表，以 `(event_id, consumer, operation_type)` 唯一键保证同一事件同一操作只消费一次，支持重复投递与安全重放。

- `consumer` 标识默认取 `spring.application.name`，可用 `cloudmall.rocketmq.consumer` 覆盖
- `operation_type` 词汇由消费方定义（如 `ORDER_PAID`）
- Mapper 通过 `MapperFactoryBean` 显式注册，不依赖 `@MapperScan`，也不影响消费方自身 Mapper 的自动扫描
- 仅在 classpath 存在 MyBatis-Plus 时装配；纯发送场景（无 MP）自动跳过

### 3. 消费模板（`AbstractMessageConsumer` + `EventHandler`）

每个 topic 一个薄消费者子类，声明 `@RocketMQMessageListener` 并传入载荷类型与处理器集合：

```java
@Component
@RocketMQMessageListener(
        topic = "ORDER_EVENTS",
        consumerGroup = "coupon-order-events",
        selectorType = SelectorType.TAG,
        selectorExpression = "OrderPaid || OrderTimeout || OrderCancelled || OrderFullyRefunded"
)
public class OrderEventConsumer extends AbstractMessageConsumer<OrderEvent> {

    public OrderEventConsumer(IMessageConsumeService consumeService, List<EventHandler<OrderEvent>> handlers) {
        super(consumeService, handlers, OrderEvent.class);
    }
}
```

业务处理实现 `EventHandler<T>`，按 tag 分发：

```java
@Component
@RequiredArgsConstructor
public class OrderPaidHandler implements EventHandler<OrderEvent> {

    private final CouponOrderClient couponOrderClient;

    @Override
    public Set<String> tagSet() {
        return Set.of(OrderTagEnum.PAID.getTag());
    }

    @Override
    public boolean canHandle(String tag) {
        return tagSet().contains(tag);
    }

    @Override
    public String eventId(OrderEvent event) {
        return event.getEventId();
    }

    @Override
    public void handle(OrderEvent event) {
        couponOrderClient.commitByOrder(
                CouponOrderCommitByOrderReq.builder()
                                           .orderNo(event.getOrderNo())
                                           .build()
        );
    }
}
```

消费模板统一完成：JSON 解析 → tag 归一 → 按 `canHandle` 分发 → `alreadyConsumed` 幂等守卫 → `handle` 业务处理 → `markConsumed` 记账；处理抛异常时不记账，交由 RocketMQ 重试投递。

## 配置项

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: cloudmall-producer

cloudmall:
  rocketmq:
    enabled: true             # 发送客户端装配开关（默认 true）
    consumer: order-service   # 消费幂等消费者标识（默认取 spring.application.name）
```

## 自动装配

- `MqAutoConfiguration`：classpath 存在 `RocketMQTemplate` 且 `cloudmall.rocketmq.enabled != false` 时装配 `MQProducer`（`@ConditionalOnMissingBean`）
- `MessageConsumeAutoConfiguration`：classpath 存在 MyBatis-Plus `BaseMapper` 时装配幂等 Mapper 与 `MessageConsumeService`

## 依赖关系

```
cloudmall-rocketmq-spring-boot-starter
├── rocketmq-spring-boot-starter（RocketMQTemplate）
├── cloudmall-common（Asserts、BizError）
└── cloudmall-mybatis-plus-spring-boot-starter（optional，仅消费幂等台账使用，消费方需显式引入）
```

## 注意事项

- 幂等台账依赖 `cloudmall-mybatis-plus-spring-boot-starter`，pom 中为 optional 依赖，使用幂等能力的服务必须显式引入，否则 `MessageConsumeService` 不装配。
- 消费处理必须自身幂等：幂等台账兜底依赖业务先幂等，重复投递由唯一键跳过。
- 消费失败抛异常不记账，RocketMQ 按消费端配置重试；`ConsumeStatusEnum` 预留 `DEAD` 死信状态。
