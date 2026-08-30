package com.cloudmall.product.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.product.api.response.CommentResp;
import com.cloudmall.product.api.response.CommentSummaryResp;
import com.cloudmall.product.api.response.StarDistributionResp;
import com.cloudmall.product.convert.CommentConverter;
import com.cloudmall.product.entity.CommentDO;
import com.cloudmall.product.mapper.CommentMapper;
import com.cloudmall.product.model.req.CommentPageReq;
import com.cloudmall.product.service.ICommentService;
import com.cloudmall.user.api.client.InternalUserClient;
import com.cloudmall.user.api.response.UserResp;

/**
 * 评论应用服务
 * <p>
 * 编排商品评论查询：总体统计在应用层计算，分页结果按用户昵称回填展示信息。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class CommentApp {

    /** 匿名用户展示名 */
    private static final String ANONYMOUS_USERNAME = "匿名用户";

    /** 百分比换算基数 */
    private static final int PERCENT_BASE = 100;

    /** 保留小数位数 */
    private static final int SCALE = 1;

    private final CommentMapper commentMapper;
    private final ICommentService commentService;
    private final CommentConverter commentConverter;
    private final InternalUserClient internalUserClient;

    /**
     * 查询商品评论总体统计。
     *
     * @param productId 商品 ID
     * @return 评论统计
     */
    public CommentSummaryResp getSummary(Long productId) {
        Asserts.notNull(productId, BizError.PRODUCT_ID_INVALID);
        List<CommentDO> comments = commentMapper.selectVisibleByProduct(productId);
        return buildSummary(comments);
    }

    /**
     * 分页查询商品评论并回填用户昵称。
     *
     * @param req 分页查询条件
     * @return 评论分页结果
     */
    public IPage<CommentResp> page(CommentPageReq req) {
        Asserts.notNull(req.getProductId(), BizError.PRODUCT_ID_INVALID);
        IPage<CommentResp> page = commentService.page(req);
        fillUsernames(page.getRecords());
        return page;
    }

    /**
     * 基于商品全部可见评论计算总体统计。
     *
     * @param comments 评论列表
     * @return 评论统计
     */
    private CommentSummaryResp buildSummary(List<CommentDO> comments) {
        int total = comments.size();
        long goodCount = comments.stream()
                                 .filter(comment -> isGood(comment.getRating()))
                                 .count();
        long neutralCount = comments.stream()
                                    .filter(comment -> isNeutral(comment.getRating()))
                                    .count();
        long badCount = comments.stream()
                                .filter(comment -> isBad(comment.getRating()))
                                .count();
        long hasImageCount = comments.stream()
                                     .filter(CommentApp::hasImage)
                                     .count();
        return CommentSummaryResp.builder()
                                 .avgRating(calculateAvgRating(comments, total))
                                 .totalCount((long) total)
                                 .starDistribution(buildStarDistribution(comments, total))
                                 .goodCount(goodCount)
                                 .neutralCount(neutralCount)
                                 .badCount(badCount)
                                 .hasImageCount(hasImageCount)
                                 .build();
    }

    /**
     * 计算综合评分，保留 1 位小数。
     *
     * @param comments 评论列表
     * @param total 评论总数
     * @return 综合评分
     */
    private BigDecimal calculateAvgRating(List<CommentDO> comments, int total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        long sum = 0;
        for (CommentDO comment : comments) {
            if (comment.getRating() != null) {
                sum += comment.getRating();
            }
        }
        return BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(total), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 构建 5 到 1 星固定顺序的星级分布。
     *
     * @param comments 评论列表
     * @param total 评论总数
     * @return 星级分布列表
     */
    private List<StarDistributionResp> buildStarDistribution(List<CommentDO> comments, int total) {
        long[] counts = new long[6];
        for (CommentDO comment : comments) {
            if (comment.getRating() != null) {
                counts[comment.getRating()]++;
            }
        }

        List<StarDistributionResp> distribution = new ArrayList<>(5);
        for (int rating = 5; rating >= 1; rating--) {
            long count = counts[rating];
            distribution.add(
                    StarDistributionResp.builder()
                                        .rating(rating)
                                        .count(count)
                                        .percent(calculatePercent(count, total))
                                        .build()
            );
        }
        return distribution;
    }

    /**
     * 计算星级占比，保留 1 位小数。
     *
     * @param count 星级评论数
     * @param total 评论总数
     * @return 占比百分数
     */
    private BigDecimal calculatePercent(long count, int total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(count)
                         .multiply(BigDecimal.valueOf(PERCENT_BASE))
                         .divide(BigDecimal.valueOf(total), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 回填评论用户昵称，无昵称统一显示匿名用户。
     *
     * @param comments 评论列表
     */
    private void fillUsernames(List<CommentResp> comments) {
        if (CollUtil.isEmpty(comments)) {
            return;
        }
        List<Long> userIds = comments.stream()
                                     .map(CommentResp::getUserId)
                                     .filter(Objects::nonNull)
                                     .distinct()
                                     .toList();
        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        Map<Long, String> usernameMap = loadUsernameMap(userIds);
        for (CommentResp comment : comments) {
            String username = usernameMap.get(comment.getUserId());
            if (StrUtil.isBlank(username)) {
                username = ANONYMOUS_USERNAME;
            }
            commentConverter.fillUsername(comment, username);
        }
    }

    /**
     * 批量加载用户 ID 到昵称的映射。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 到昵称映射
     */
    private Map<Long, String> loadUsernameMap(List<Long> userIds) {
        List<UserResp> users = internalUserClient.listByIds(userIds);
        Map<Long, String> usernameMap = new HashMap<>();
        for (UserResp user : users) {
            usernameMap.put(user.getUserId(), user.getNickname());
        }
        return usernameMap;
    }

    /**
     * 判断评分是否属于好评（4-5 星）。
     *
     * @param rating 评分
     * @return true 表示好评
     */
    private static boolean isGood(Integer rating) {
        return rating != null && rating >= 4;
    }

    /**
     * 判断评分是否属于中评（3 星）。
     *
     * @param rating 评分
     * @return true 表示中评
     */
    private static boolean isNeutral(Integer rating) {
        return rating != null && rating == 3;
    }

    /**
     * 判断评分是否属于差评（1-2 星）。
     *
     * @param rating 评分
     * @return true 表示差评
     */
    private static boolean isBad(Integer rating) {
        return rating != null && rating <= 2;
    }

    /**
     * 判断评论是否包含图片。
     *
     * @param comment 评论
     * @return true 表示包含图片
     */
    private static boolean hasImage(CommentDO comment) {
        return StrUtil.isNotBlank(comment.getImages());
    }
}
