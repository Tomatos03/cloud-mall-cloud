# cloudmall-rocketmq-spring-boot-starter

RocketMQ 消息基础设施 starter，提供通用发送客户端 `MQProducer` 与事务消息 header 常量。消费者相关抽象（`AbstractMessageConsumer` / `EventHandler`）已下沉到各消费方服务（如 consumer-service），不再由本 starter 提供。

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

### 2. 事务消息（`TxMessageHeader`）

事务消息发送与事务监听器处于同一微服务进程，通过公共 header 传递业务上下文：

| Header | 值 | 用途 |
|--------|----|------|
| `EVENT_ID` | 业务主键（orderNo / productId / userId） | 事务监听器回查定位业务记录，消费方幂等键 |
| `EVENT_TYPE` | tag 值 | 事务监听器区分事件类型 |
| `TRANSACTION_ID` | UUID | Broker 事务状态关联与日志追踪 |

事务消息发送：

```java
mqProducer.sendInTransaction("ORDER_EVENTS", OrderTagEnum.PAID.getTag(), event, orderNo);
```

- 自动装配 `TRANSACTION_ID`（UUID）、`EVENT_ID`（业务主键）、`EVENT_TYPE`（tag）
- 事务监听器实现 `RocketMQLocalTransactionListener`，通过 `TxMessageHeader` 常量读取 header 完成本地事务执行与回查
- 框架回调参数 `arg` 固定传 null，业务上下文统一从 header 读取

## 配置项

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: cloudmall-producer

cloudmall:
  rocketmq:
    enabled: true   # 发送客户端装配开关（默认 true）
```

## 自动装配

- `MqAutoConfiguration`：classpath 存在 `RocketMQTemplate` 且 `cloudmall.rocketmq.enabled != false` 时装配 `MQProducer`（`@ConditionalOnMissingBean`）

## 依赖关系

```
cloudmall-rocketmq-spring-boot-starter
├── rocketmq-spring-boot-starter（RocketMQTemplate）
├── cloudmall-common（Asserts、BizErrorEnum）
└── lombok（optional，编译期）
```

## 注意事项

- 事务消息依赖事务监听器（`@RocketMQTransactionListener`）实现本地事务与回查，监听器与发送端必须处于同一进程。
- 消费失败由 RocketMQ 重试机制兜底（`maxReconsumeTimes` 次后进入死信队列 `%DLQ%{consumerGroup}`），消费方应监听死信队列并告警。
- 消费处理必须自身幂等：重复投递由业务层幂等（唯一约束 / 幂等键）保障。
