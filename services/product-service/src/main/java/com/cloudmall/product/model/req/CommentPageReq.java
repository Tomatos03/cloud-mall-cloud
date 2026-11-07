package com.cloudmall.product.model.req;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;
import com.cloudmall.mybatisplus.enums.StatusEnum;
import com.cloudmall.mybatisplus.model.PageReq;
import com.cloudmall.product.model.entity.CommentDO;
import com.cloudmall.product.enums.CommentRatingTypeEnum;
import com.cloudmall.product.enums.CommentSortEnum;

/**
 * 评论分页查询请求。
 * <p>
 * 通过 {@link QueryCondition @QueryCondition} 注解声明筛选条件，
 * 调用 {@link #buildLambdaWrapper()} 自动生成查询条件。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageReq extends PageReq<CommentDO> {

    /** 好评最低评分 */
    private static final int GOOD_RATING_MIN = 4;

    /** 中评评分 */
    private static final int MIDDLE_RATING = 3;

    /** 差评最高评分 */
    private static final int BAD_RATING_MAX = 2;

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

    /**
     * 构建所需对象。
     *
     * @return 处理结果
     */
    @Override
    public LambdaQueryWrapper<CommentDO> buildLambdaWrapper() {
        LambdaQueryWrapper<CommentDO> wrapper = super.buildLambdaWrapper();
        applyRatingType(wrapper);
        applyHasImage(wrapper);
        applySort(wrapper);
        return wrapper;
    }

    /**
     * 按评分筛选类型追加评分区间条件。
     *
     * @param wrapper 查询条件
     */
    private void applyRatingType(LambdaQueryWrapper<CommentDO> wrapper) {
        if (ratingType == null || ratingType == CommentRatingTypeEnum.ALL) {
            return;
        }
        switch (ratingType) {
            case GOOD -> wrapper.ge(CommentDO::getRating, GOOD_RATING_MIN);
            case MIDDLE -> wrapper.eq(CommentDO::getRating, MIDDLE_RATING);
            case BAD -> wrapper.le(CommentDO::getRating, BAD_RATING_MAX);
            default -> throw new IllegalArgumentException("不支持的评论评分筛选类型: " + ratingType);
        }
    }

    /**
     * 按有图筛选追加图片非空条件。
     *
     * @param wrapper 查询条件
     */
    private void applyHasImage(LambdaQueryWrapper<CommentDO> wrapper) {
        if (!BooleanUtil.isTrue(hasImage)) {
            return;
        }
        wrapper.isNotNull(CommentDO::getImages).ne(CommentDO::getImages, "");
    }

    /**
     * 按排序方式追加排序条件。
     *
     * @param wrapper 查询条件
     */
    private void applySort(LambdaQueryWrapper<CommentDO> wrapper) {
        CommentSortEnum sort = ObjectUtil.defaultIfNull(sortType, CommentSortEnum.DEFAULT);
        switch (sort) {
            case LATEST -> wrapper.orderByDesc(CommentDO::getCreateTime);
            case HOTTEST -> wrapper.orderByDesc(CommentDO::getUsefulCount)
                                   .orderByDesc(CommentDO::getCreateTime);
            default -> wrapper.orderByDesc(CommentDO::getRating).orderByDesc(CommentDO::getCreateTime);
        }
    }
}
