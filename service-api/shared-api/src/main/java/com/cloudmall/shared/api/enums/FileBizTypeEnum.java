package com.cloudmall.shared.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件所属业务类型。
 *
 * @author : Tomatos
 */
@Getter
@AllArgsConstructor
public enum FileBizTypeEnum {
    /** 通用上传，业务含义在绑定时确定 */
    GENERAL("GENERAL", "通用"),

    /** 用户头像 */
    AVATAR("AVATAR", "用户头像"),

    /** 商家入驻资料 */
    SHOP_ONBOARDING("SHOP_ONBOARDING", "商家入驻"),

    /** 聊天图片或视频 */
    CHAT("CHAT", "聊天文件");
    /** 数据库存储值 */
    @EnumValue
    private final String code;
    /** 业务类型描述 */
    private final String desc;
}
