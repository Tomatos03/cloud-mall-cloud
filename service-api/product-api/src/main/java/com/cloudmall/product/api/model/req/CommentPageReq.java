package com.cloudmall.product.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.enums.StatusEnum;
import com.cloudmall.common.model.page.PageReq;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.product.api.enums.CommentRatingTypeEnum;
import com.cloudmall.product.api.enums.CommentSortEnum;

/**
 * 评论分页查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件；评分、有图与
 * 排序等特殊条件由 {@code CommentQueryHelper} 在 Mapper 中追加。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageReq extends PageReq {

    /** 商品 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long productId;

    /** 评分筛选（全部/好评/中评/差评） */
    private CommentRatingTypeEnum ratingType;

    /** 是否有图 */
    private Boolean hasImage;

    /** 排序方式（默认/最新/最热） */
    private CommentSortEnum sortType;

    /** 状态（等值匹配），默认查询已启用评论 */
    @QueryCondition(operator = OperatorEnum.EQ)
    private StatusEnum status = StatusEnum.ENABLED;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public CommentPageReq(long current, long size) {
        super(current, size);
    }
}
