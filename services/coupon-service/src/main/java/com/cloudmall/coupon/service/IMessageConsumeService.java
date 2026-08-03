package com.cloudmall.coupon.service;

/**
 * 消息消费幂等服务
 * <p>
 * 基于 cm_message_consume_record 唯一键保证同一事件同一操作只消费一次，
 * 支持安全重放：业务幂等 + 消费记录兜底。
 *
 * @author : Tomatos
 */
public interface IMessageConsumeService {

    /** 消费者标识 */
    String CONSUMER = "coupon-service";

    /** 注册自动发放新人券 */
    String OP_REGISTER_AUTO_ISSUE = "REGISTER_AUTO_ISSUE";

    /** 订单支付核销 */
    String OP_ORDER_PAID = "ORDER_PAID";

    /** 订单超时释放 */
    String OP_ORDER_TIMEOUT = "ORDER_TIMEOUT";

    /** 订单取消释放 */
    String OP_ORDER_CANCELLED = "ORDER_CANCELLED";

    /** 订单全额退款回退 */
    String OP_ORDER_REFUNDED = "ORDER_REFUNDED";

    /**
     * 判断事件是否已消费。
     *
     * @param eventId 事件 ID
     * @param operationType 操作类型
     * @return true 表示已消费
     */
    boolean alreadyConsumed(String eventId, String operationType);

    /**
     * 标记事件消费成功（唯一键冲突忽略，表示并发已消费）。
     *
     * @param eventId 事件 ID
     * @param operationType 操作类型
     */
    void markConsumed(String eventId, String operationType);
}
