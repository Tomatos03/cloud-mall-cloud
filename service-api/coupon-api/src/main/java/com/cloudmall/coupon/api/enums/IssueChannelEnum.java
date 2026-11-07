package com.cloudmall.coupon.api.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券发放渠道枚举
 * <p>
 * 对应 cm_coupon.issue_channel 列的取值集合：公开领取由用户在券中心主动领取，
 * 注册自动发放由注册事件触发平台新人券。模板未设置渠道（null）时不可发布。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum IssueChannelEnum {
    /** 公开领取 — 用户从券中心主动领取 */
    PUBLIC_CLAIM("PUBLIC_CLAIM", "公开领取"),

    /** 注册自动发放 — 注册事件触发的平台新人券 */
    REGISTER_AUTO("REGISTER_AUTO", "注册自动发放");
    /** 数据库存储值 */
    @EnumValue
    @JsonValue
    private final String code;
    /** 渠道描述 */
    private final String desc;
    /**
     * 根据编码查找发放渠道。
     *
     * @param code 渠道编码，如 "PUBLIC_CLAIM"
     * @return 匹配的发放渠道
     * @throws IllegalArgumentException 编码非法或为 null 时抛出
     */
    @JsonCreator
    public static IssueChannelEnum fromCode(@JsonProperty("code") String code) {
        return Arrays
                .stream(values())
                .filter(t -> t.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Invalid issue channel: '" + code + "', valid values: "
                                        + Arrays.toString(values())
                        )
                );
    }
}
