package com.cloudmall.seckill.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.seckill.enums.SeckillProductAuditStatusEnum;
import com.cloudmall.seckill.model.entity.SeckillProductDO;
import com.cloudmall.seckill.model.req.SeckillProductPageReq;

/**
 * 秒杀商品 Mapper
 *
 * @author : Tomatos
 */
@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProductDO> {

    /**
     * 分页查询秒杀商品，按 ID 倒序。 <p>
     * 条件由 {@link SeckillProductPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return 商品分页数据
     */
    default IPage<SeckillProductDO> selectProductPage(SeckillProductPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 查询活动中已审核通过的商品。
     *
     * @param activityId 活动 ID
     * @return 已审核通过的商品列表
     */
    default List<SeckillProductDO> listApprovedByActivityId(Long activityId) {
        return selectList(
                Wrappers.<SeckillProductDO>lambdaQuery()
                        .eq(SeckillProductDO::getActivityId, activityId)
                        .eq(SeckillProductDO::getAuditStatus, SeckillProductAuditStatusEnum.APPROVED)
        );
    }

    /**
     * 查询活动中某 SKU 已审核通过的商品。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     * @return 已审核通过的商品，不存在时返回 null
     */
    default SeckillProductDO selectApprovedByActivityAndSku(Long activityId, Long skuId) {
        return selectOne(
                Wrappers.<SeckillProductDO>lambdaQuery()
                        .eq(SeckillProductDO::getActivityId, activityId)
                        .eq(SeckillProductDO::getSkuId, skuId)
                        .eq(SeckillProductDO::getAuditStatus, SeckillProductAuditStatusEnum.APPROVED)
        );
    }

    /**
     * 按 ID 查询已审核通过的商品。
     *
     * @param id 秒杀商品 ID
     * @return 已审核通过的商品，不存在时返回 null
     */
    default SeckillProductDO selectApprovedById(Long id) {
        return selectOne(
                Wrappers.<SeckillProductDO>lambdaQuery()
                        .eq(SeckillProductDO::getId, id)
                        .eq(SeckillProductDO::getAuditStatus, SeckillProductAuditStatusEnum.APPROVED)
        );
    }

    /**
     * 查询活动下的全部商品。
     *
     * @param activityId 活动 ID
     * @return 商品列表
     */
    default List<SeckillProductDO> listByActivityId(Long activityId) {
        return selectList(
                Wrappers.<SeckillProductDO>lambdaQuery().eq(SeckillProductDO::getActivityId, activityId)
        );
    }

    /**
     * 查询店铺提交的全部商品（按 ID 倒序）。
     *
     * @param shopId 店铺 ID
     * @return 商品列表
     */
    default List<SeckillProductDO> listByShopId(Long shopId) {
        return selectList(
                Wrappers.<SeckillProductDO>lambdaQuery()
                        .eq(SeckillProductDO::getShopId, shopId)
                        .orderByDesc(SeckillProductDO::getId)
        );
    }

    /**
     * 统计指定活动中某 SKU 的商品数（同活动同 SKU 唯一校验）。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     * @return 匹配记录数
     */
    default Long countByActivityAndSku(Long activityId, Long skuId) {
        return selectCount(
                Wrappers.<SeckillProductDO>lambdaQuery()
                        .eq(SeckillProductDO::getActivityId, activityId)
                        .eq(SeckillProductDO::getSkuId, skuId)
        );
    }

    /**
     * 统计指定活动下的商品数。
     *
     * @param activityId 活动 ID
     * @return 商品数
     */
    default Long countByActivityId(Long activityId) {
        return selectCount(
                Wrappers.<SeckillProductDO>lambdaQuery().eq(SeckillProductDO::getActivityId, activityId)
        );
    }

    /**
     * 扣减秒杀池库存并累加销量（带库存充足条件防负库存）。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     * @return 影响行数，0 表示库存不足
     */
    default int deductStock(Long activityId, Long skuId) {
        return update(
                Wrappers.<SeckillProductDO>lambdaUpdate()
                        .setSql("stock = stock - 1, sold_count = sold_count + 1")
                        .eq(SeckillProductDO::getActivityId, activityId)
                        .eq(SeckillProductDO::getSkuId, skuId)
                        .ge(SeckillProductDO::getStock, 1)
        );
    }

    /**
     * 回补秒杀池库存并减回销量（普通流程建单失败时补偿）。
     *
     * @param activityId 活动 ID
     * @param skuId SKU ID
     */
    default void releaseStock(Long activityId, Long skuId) {
        update(
                Wrappers.<SeckillProductDO>lambdaUpdate()
                        .setSql("stock = stock + 1, sold_count = GREATEST(sold_count - 1, 0)")
                        .eq(SeckillProductDO::getActivityId, activityId)
                        .eq(SeckillProductDO::getSkuId, skuId)
        );
    }

    /**
     * 查询指定活动中已审核通过的商品。 <p>
     * 条件：活动 ID、商品 ID 匹配且审核状态为 APPROVED。
     *
     * @param activityId 活动 ID
     * @param productId 秒杀商品 ID
     * @return 已审核通过的商品，不存在时返回 null
     */
    default SeckillProductDO selectApprovedProduct(Long activityId, Long productId) {
        return selectOne(
                Wrappers.<SeckillProductDO>lambdaQuery()
                        .eq(SeckillProductDO::getActivityId, activityId)
                        .eq(SeckillProductDO::getId, productId)
                        .eq(SeckillProductDO::getAuditStatus, SeckillProductAuditStatusEnum.APPROVED)
        );
    }
}
