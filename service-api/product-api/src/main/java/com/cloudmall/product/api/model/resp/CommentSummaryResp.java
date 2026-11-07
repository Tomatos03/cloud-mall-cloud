package com.cloudmall.product.api.model.resp;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品评论总体统计响应。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSummaryResp {

    /** 综合评分（保留 1 位小数） */
    private BigDecimal avgRating;

    /** 评价总数 */
    private Long totalCount;

    /** 星级分布（5 到 1 星固定顺序） */
    private List<StarDistributionResp> starDistribution;

    /** 好评数量（4-5 星） */
    private Long goodCount;

    /** 中评数量（3 星） */
    private Long neutralCount;

    /** 差评数量（1-2 星） */
    private Long badCount;

    /** 有图评论数量 */
    private Long hasImageCount;
}
