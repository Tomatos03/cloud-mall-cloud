package com.cloudmall.user.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.api.model.resp.ShopInfoResp;
import com.cloudmall.user.api.model.resp.ShopResp;
import com.cloudmall.user.convert.ShopConverter;
import com.cloudmall.user.model.entity.ShopDO;
import com.cloudmall.user.model.entity.ShopStatDO;
import com.cloudmall.user.mapper.ShopMapper;
import com.cloudmall.user.mapper.ShopStatMapper;
import com.cloudmall.user.model.req.ShopReq;
import com.cloudmall.user.service.IShopService;

@Service
@RequiredArgsConstructor
public class ShopService implements IShopService {

    /** 好评率换算基数 */
    private static final int PERCENT_BASE = 100;

    /** 保留小数位数 */
    private static final int SCALE = 1;

    private final ShopMapper shopMapper;
    private final ShopConverter shopConverter;
    private final ShopStatMapper shopStatMapper;

    /**
     * 获取相关数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @Override
    public ShopResp get(ShopReq req) {
        ShopDO shop = shopMapper.selectByReq(req);
        return shopConverter.toResp(shop);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Long> findShopIdByUserId(@NonNull Long userId) {
        ShopDO shop = shopMapper.selectByReq(ShopReq.builder()
                                                    .userId(userId)
                                                    .build());
        return Optional.ofNullable(shop).map(ShopDO::getId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShopInfoResp getShopInfo(Long shopId) {
        Asserts.notNull(shopId, BizErrorEnum.INVALID_PARAM);
        ShopDO shop = Asserts.requireNonNull(
                shopMapper.selectByReq(ShopReq.builder()
                                              .id(shopId)
                                              .build()),
                BizErrorEnum.MERCHANT_STORE_NOT_FOUND
        );
        ShopStatDO stat = shopStatMapper.selectById(shopId);
        long totalReviewCount = stat == null ? 0 : stat.getTotalReviewCount();
        long productCount = stat == null ? 0 : stat.getProductCount();
        long followCount = stat == null ? 0 : stat.getFollowCount();
        ShopInfoResp resp = shopConverter.toInfoResp(shop);
        return shopConverter.applyStat(
                resp,
                calculateRating(totalScore(stat), totalReviewCount),
                productCount,
                calculatePositiveRate(positiveReviewCount(stat), totalReviewCount),
                followCount
        );
    }

    /**
     * 计算店铺评分，保留 1 位小数。
     *
     * @param totalScore 评价总评分
     * @param totalReviewCount 有效评价数量
     * @return 评分
     */
    private BigDecimal calculateRating(long totalScore, long totalReviewCount) {
        if (totalReviewCount == 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(totalScore)
                         .divide(BigDecimal.valueOf(totalReviewCount), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 计算好评率，保留 1 位小数。
     *
     * @param positiveReviewCount 好评数量
     * @param totalReviewCount 有效评价数量
     * @return 好评率百分数
     */
    private BigDecimal calculatePositiveRate(long positiveReviewCount, long totalReviewCount) {
        if (totalReviewCount == 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(positiveReviewCount)
                         .multiply(BigDecimal.valueOf(PERCENT_BASE))
                         .divide(BigDecimal.valueOf(totalReviewCount), SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 读取评价总评分，统计缺失时按 0 处理。
     *
     * @param stat 店铺统计
     * @return 评价总评分
     */
    private static long totalScore(ShopStatDO stat) {
        return stat == null ? 0 : stat.getTotalScore();
    }

    /**
     * 读取好评数量，统计缺失时按 0 处理。
     *
     * @param stat 店铺统计
     * @return 好评数量
     */
    private static long positiveReviewCount(ShopStatDO stat) {
        return stat == null ? 0 : stat.getPositiveReviewCount();
    }

    /**
     * 查询数据列表。
     *
     * @param shopIds 标识列表
     * @return 数据列表
     */
    @Override
    public List<ShopResp> listByIds(List<Long> shopIds) {
        if (CollUtil.isEmpty(shopIds)) {
            return Collections.emptyList();
        }
        List<ShopDO> shops = shopMapper.selectByIds(shopIds);
        return shopConverter.toRespList(shops);
    }
}
