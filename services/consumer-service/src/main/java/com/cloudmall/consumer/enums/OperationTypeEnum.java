package com.cloudmall.consumer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消费幂等操作类型枚举
 * <p>
 * 对应 cm_message_consume_record.operation_type 的有限值集合，
 * 由各事件处理器在 tags() 映射中引用。新增处理器时在此补充枚举值。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum OperationTypeEnum {
    /** 注册自动发放新人券 */
    REGISTER_AUTO_ISSUE("REGISTER_AUTO_ISSUE", "注册自动发放新人券"),

    /** 订单支付核销 */
    ORDER_PAID("ORDER_PAID", "订单支付核销"),

    /** 订单超时释放 */
    ORDER_TIMEOUT("ORDER_TIMEOUT", "订单超时释放"),

    /** 订单取消释放 */
    ORDER_CANCELLED("ORDER_CANCELLED", "订单取消释放"),

    /** 订单全额退款回退 */
    ORDER_REFUNDED("ORDER_REFUNDED", "订单全额退款回退");
    /** 操作类型码（台账存储值） */
    private final String code;
    /** 操作描述 */
    private final String desc;
}
