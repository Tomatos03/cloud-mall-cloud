package com.cloudmall.coupon.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户券流水操作类型枚举
 * <p>
 * 对应 cm_user_coupon_log.operation_type 列的取值集合，
 * 与 (event_id, operation_type) 唯一约束共同构成操作幂等依据。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum UserCouponOperationTypeEnum {

    /** 发放 — 公开领取或注册自动发放 */
    ISSUE("ISSUE", "发放"),

    /** 锁定 — 订单创建时锁定 */
    LOCK("LOCK", "锁定"),

    /** 提交核销 — 支付成功后核销 */
    COMMIT("COMMIT", "提交"),

    /** 释放 — 取消/超时/锁定到期释放 */
    RELEASE("RELEASE", "释放"),

    /** 回退 — 全额退款且未过期时回退为可用 */
    REVERT("REVERT", "回退"),

    /** 过期 — 批处理任务批量置为过期 */
    EXPIRE("EXPIRE", "过期"),

    /** 撤销 — 运营紧急撤销 */
    REVOKE("REVOKE", "撤销");

    /** 数据库存储值 */
    @EnumValue
    private final String code;

    /** 操作描述 */
    private final String desc;

    /**
     * 根据编码查找操作类型。
     *
     * @param code 操作编码，如 "LOCK"
     * @return 匹配的操作类型
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static UserCouponOperationTypeEnum fromCode(String code) {
        return Arrays.stream(values())
                     .filter(t -> t.code.equals(code))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException(
                             "Invalid user coupon operation type: '" + code + "', valid values: "
                                     + Arrays.toString(values())
                     ));
    }
}
