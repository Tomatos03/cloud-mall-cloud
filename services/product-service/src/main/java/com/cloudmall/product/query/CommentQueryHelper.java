package com.cloudmall.product.query;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.cloudmall.product.api.enums.CommentRatingTypeEnum;
import com.cloudmall.product.api.enums.CommentSortEnum;
import com.cloudmall.product.api.model.req.CommentPageReq;
import com.cloudmall.product.model.entity.CommentDO;

/**
 * 评论查询条件补充构建器。
 * <p>
 * 评分区间、有图筛选与排序无法用 {@code @QueryCondition} 表达，
 * 由本工具类在 Mapper 构建完注解条件后追加。
 *
 * @author : Tomatos
 */
public final class CommentQueryHelper {

    /** 好评最低评分 */
    private static final int GOOD_RATING_MIN = 4;

    /** 中评评分 */
    private static final int MIDDLE_RATING = 3;

    /** 差评最高评分 */
    private static final int BAD_RATING_MAX = 2;

    /**
     * 追加评论特有查询条件。
     *
     * @param req 分页请求
     * @param wrapper 查询条件
     */
    public static void apply(CommentPageReq req, LambdaQueryWrapper<CommentDO> wrapper) {
        applyRatingType(req, wrapper);
        applyHasImage(req, wrapper);
        applySort(req, wrapper);
    }

    private static void applyRatingType(CommentPageReq req, LambdaQueryWrapper<CommentDO> wrapper) {
        CommentRatingTypeEnum ratingType = req.getRatingType();
        if (ratingType == null || ratingType == CommentRatingTypeEnum.ALL) {
            return;
        }
        switch (ratingType) {
            case ALL -> {
            }
            case GOOD -> wrapper.ge(CommentDO::getRating, GOOD_RATING_MIN);
            case MIDDLE -> wrapper.eq(CommentDO::getRating, MIDDLE_RATING);
            case BAD -> wrapper.le(CommentDO::getRating, BAD_RATING_MAX);
        }
    }

    private static void applyHasImage(CommentPageReq req, LambdaQueryWrapper<CommentDO> wrapper) {
        if (!BooleanUtil.isTrue(req.getHasImage())) {
            return;
        }
        wrapper.isNotNull(CommentDO::getImages).ne(CommentDO::getImages, "");
    }

    private static void applySort(CommentPageReq req, LambdaQueryWrapper<CommentDO> wrapper) {
        CommentSortEnum sort = ObjectUtil.defaultIfNull(req.getSortType(), CommentSortEnum.DEFAULT);
        switch (sort) {
            case LATEST -> wrapper.orderByDesc(CommentDO::getCreateTime);
            case HOTTEST -> wrapper.orderByDesc(CommentDO::getUsefulCount)
                                   .orderByDesc(CommentDO::getCreateTime);
            default -> wrapper.orderByDesc(CommentDO::getRating).orderByDesc(CommentDO::getCreateTime);
        }
    }

    private CommentQueryHelper() {}
}
