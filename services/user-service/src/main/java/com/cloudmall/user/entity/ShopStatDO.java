package com.cloudmall.user.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺统计实体。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cm_shop_stat")
public class ShopStatDO {

    /** 店铺 ID */
    private Long shopId;

    /** 出售中商品数量 */
    private Integer productCount;

    /** 有效评价数量 */
    private Integer totalReviewCount;

    /** 好评数量 */
    private Integer positiveReviewCount;

    /** 评价总评分 */
    private Integer totalScore;

    /** 关注数量 */
    private Long followCount;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
