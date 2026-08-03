package com.cloudmall.coupon.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券商品范围实体
 * <p>
 * 对应 cm_coupon_product 表，scope_type=PRODUCT 时按 SPU 配置，
 * 覆盖该 SPU 下全部 SKU。联合主键 (coupon_id, product_id)。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_coupon_product")
public class CouponProductDO {

    /** 模板 ID */
    private Long couponId;

    /** 商品 SPU ID */
    private Long productId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
