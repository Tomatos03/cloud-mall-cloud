package com.cloudmall.product.api.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论星级分布响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarDistributionResp {

    /** 星级（1-5） */
    private Integer rating;

    /** 该星级评论数量 */
    private Long count;

    /** 占比（百分数，保留 1 位小数） */
    private BigDecimal percent;
}
