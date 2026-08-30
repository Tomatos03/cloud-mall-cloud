package com.cloudmall.order.api.request;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.order.api.model.OrderItem;

/**
 * 创建订单请求
 * <p>
 * 三条建单链路共用：购物车结算、直接购买、秒杀建单，结算类型通过路径参数传递。
 * 普通与直接购买只提交 SKU 与数量、用户券选择、地址和备注；
 * userId、价格、商品名称、店铺、分类、优惠金额等一律由服务端从上下文与可信快照组装。
 * 秒杀由 consumer-service 内部调用，携带 orderNo，其余建单信息从秒杀单快照推断。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderReq {

    /** 下单商品明细 */
    @Valid
    @NotEmpty
    private List<OrderItem> items;

    /** 用户选择的优惠券 ID 列表（可空，不传时不使用优惠券） */
    private List<Long> userCouponIds;

    /** 收货地址 ID */
    @NotNull
    private Long addressId;

    /** 订单备注（单店铺或未按店铺拆分时使用） */
    private String remark;

    /** 店铺维度备注（跨店铺聚合订单按店铺单独设置，key 为店铺 ID，可空） */
    private Map<Long, String> shopRemarks;

    /** 订单号（仅秒杀，由秒杀侧预生成，uk_order_no 幂等键） */
    private String orderNo;
}
