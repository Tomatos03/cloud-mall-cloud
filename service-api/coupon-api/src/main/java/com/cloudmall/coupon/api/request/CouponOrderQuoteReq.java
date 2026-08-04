package com.cloudmall.coupon.api.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.cloudmall.coupon.api.dto.CouponCalcItem;

/**
 * 优惠券试算请求
 * <p>
 * 内部接口，由订单服务在下单试算时调用，无副作用。
 * 用户身份显式传入（异步场景无用户上下文），不信任客户端金额。
 * 用户选择的优惠券以 ID 列表传入，由服务端内部区分平台券与店铺券。
 *
 * @author : Tomatos
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CouponOrderQuoteReq {

    /** 下单用户 ID（内部接口显式传入） */
    @NotNull
    private Long userId;

    /** 可信商品明细列表 */
    @Valid
    @NotEmpty
    private List<CouponCalcItem> items;

    /** 用户选择的优惠券 ID 列表（可空，不传时自动推荐最优组合） */
    private List<Long> userCouponIds;
}
