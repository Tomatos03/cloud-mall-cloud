package com.cloudmall.user.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺信息响应（含统计指标）。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopInfoResp {

    /** 店铺 ID */
    private Long id;

    /** 店铺名称 */
    private String name;

    /** 店铺 Logo URL */
    private String logo;

    /** 店铺描述 */
    private String description;

    /** 评分（星级，满星 5，保留 1 位小数，半星粒度由前端展示） */
    private BigDecimal rating;

    /** 出售中商品数量 */
    private Long productCount;

    /** 好评率（百分数，保留 1 位小数） */
    private BigDecimal positiveRate;

    /** 关注数量 */
    private Long followCount;
}
