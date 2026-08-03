package com.cloudmall.product.enums;

/**
 * 库存操作类型
 * <p>
 * RESERVE 预占（创建订单扣减可售库存）；CONFIRM 确认（支付成功，不改库存只记流水）；
 * RELEASE 释放（取消/超时回补）；COMPENSATE 补偿（恢复任务兜底回补）。
 *
 * @author : Tomatos
 */
public enum StockOpTypeEnum {
    /** 预占 */
    RESERVE,

    /** 确认 */
    CONFIRM,

    /** 释放 */
    RELEASE,

    /** 补偿 */
    COMPENSATE
}
