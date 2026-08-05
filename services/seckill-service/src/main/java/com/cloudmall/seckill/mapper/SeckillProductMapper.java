package com.cloudmall.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.seckill.entity.SeckillProductDO;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProductDO> {

    /**
     * 查询指定活动中已审核通过的商品。 <p>
     * 条件：活动 ID、商品 ID 匹配且审核状态为 APPROVED。
     *
     * @param activityId 活动 ID
     * @param productId 商品 ID
     * @return 已审核通过的商品，不存在时返回 null
     */
    default SeckillProductDO selectApprovedProduct(Long activityId, Long productId) {
        return selectOne(
                Wrappers.<SeckillProductDO>lambdaQuery()
                        .eq(SeckillProductDO::getActivityId, activityId)
                        .eq(SeckillProductDO::getProductId, productId)
                        .eq(SeckillProductDO::getAuditStatus, "APPROVED")
        );
    }
}
