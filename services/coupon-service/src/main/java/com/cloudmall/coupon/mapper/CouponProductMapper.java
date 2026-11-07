package com.cloudmall.coupon.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.model.entity.CouponProductDO;

/**
 * 优惠券商品范围 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface CouponProductMapper extends BaseMapper<CouponProductDO> {

    /**
     * 查询模板配置的商品 SPU ID 列表。
     *
     * @param couponId 模板 ID
     * @return 商品 ID 列表
     */
    default List<Long> selectIdsByCouponId(Long couponId) {
        List<CouponProductDO> products = selectList(
                Wrappers.<CouponProductDO>lambdaQuery()
                        .select(CouponProductDO::getProductId)
                        .eq(CouponProductDO::getCouponId, couponId)
        );
        return products.stream()
                       .map(CouponProductDO::getProductId)
                       .toList();
    }

    /**
     * 统计模板配置的商品数量。
     *
     * @param couponId 模板 ID
     * @return 商品数量
     */
    default long countByCouponId(Long couponId) {
        return selectCount(Wrappers.<CouponProductDO>lambdaQuery().eq(CouponProductDO::getCouponId, couponId));
    }

    /**
     * 删除模板全部商品范围关联。
     *
     * @param couponId 模板 ID
     * @return 影响行数
     */
    default int deleteByCouponId(Long couponId) {
        return delete(Wrappers.<CouponProductDO>lambdaQuery().eq(CouponProductDO::getCouponId, couponId));
    }
}
