package com.cloudmall.seckill.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀活动状态枚举
 * <p>
 * 对应 cm_seckill_activity.status 列的取值集合。
 * 发布即开放商家报名，报名截止时间为 start_time；FINISHED、CANCELLED 为终态。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum SeckillActivityStatusEnum {
    /** 草稿 — 运营排期中，商家不可见 */
    DRAFT("DRAFT", "草稿"),

    /** 报名中 — 已发布，商家可提交商品、运营可审核 */
    PUBLISHED("PUBLISHED", "报名中"),

    /** 进行中 — 秒杀窗口已开启 */
    RUNNING("RUNNING", "进行中"),

    /** 已结束 — 秒杀窗口关闭 */
    FINISHED("FINISHED", "已结束"),

    /** 已取消 — 运营主动取消，终态不可恢复 */
    CANCELLED("CANCELLED", "已取消");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 状态描述 */
    private final String desc;
    /**
     * 根据编码查找活动状态。
     *
     * @param code 状态编码，如 "RUNNING"
     * @return 匹配的活动状态
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    public static SeckillActivityStatusEnum fromCode(String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid seckill activity status: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
