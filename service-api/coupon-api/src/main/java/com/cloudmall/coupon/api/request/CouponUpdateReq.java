package com.cloudmall.coupon.api.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 编辑优惠券模板草稿请求
 * <p>
 * 仅 DRAFT 状态可编辑；首次发布后核心规则冻结，不再接受编辑。
 * 归属（ownerType）不允许修改，店铺归属在创建时已确定。
 *
 * @author : Tomatos
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CouponUpdateReq extends CouponBaseReq {
}
