package com.cloudmall.coupon.model.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 游标分页评估上下文商品项
 * <p>
 * 仅传 SKU 与数量，价格/分类等可信数据由服务端从商品服务批量获取，
 * 不信任客户端价格。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponCursorItemReq {

    /** SKU ID */
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    /** 数量 */
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于零")
    private Integer quantity;
}
