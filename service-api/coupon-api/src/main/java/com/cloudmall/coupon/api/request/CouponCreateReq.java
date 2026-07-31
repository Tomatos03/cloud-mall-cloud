package com.cloudmall.coupon.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 创建优惠券模板草稿请求
 * <p>
 * 用户端不提交 shopId：ownerType=SHOP 时由服务层从当前用户解析唯一店铺，
 * ownerType=PLATFORM 时 shopId 恒为空。发布前核心规则均可编辑，首次发布后冻结。
 *
 * @author : Tomatos
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CouponCreateReq extends CouponBaseReq {

    /** 券归属（PLATFORM / SHOP） */
    @NotBlank
    private String ownerType;
}
