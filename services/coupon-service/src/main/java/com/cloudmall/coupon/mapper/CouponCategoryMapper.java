package com.cloudmall.coupon.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.entity.CouponCategoryDO;

/**
 * 优惠券分类范围 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface CouponCategoryMapper extends BaseMapper<CouponCategoryDO> {

    /**
     * 查询模板配置的分类 ID 列表。
     *
     * @param couponId 模板 ID
     * @return 分类 ID 列表
     */
    default List<Long> selectIdsByCouponId(Long couponId) {
        List<CouponCategoryDO> categories = selectList(
                Wrappers.<CouponCategoryDO>lambdaQuery()
                        .select(CouponCategoryDO::getCategoryId)
                        .eq(CouponCategoryDO::getCouponId, couponId)
        );
        return categories.stream()
                         .map(CouponCategoryDO::getCategoryId)
                         .toList();
    }

    /**
     * 统计模板配置的分类数量。
     *
     * @param couponId 模板 ID
     * @return 分类数量
     */
    default long countByCouponId(Long couponId) {
        return selectCount(
                Wrappers.<CouponCategoryDO>lambdaQuery().eq(CouponCategoryDO::getCouponId, couponId)
        );
    }

    /**
     * 删除模板全部分类范围关联。
     *
     * @param couponId 模板 ID
     * @return 影响行数
     */
    default int deleteByCouponId(Long couponId) {
        return delete(Wrappers.<CouponCategoryDO>lambdaQuery().eq(CouponCategoryDO::getCouponId, couponId));
    }
}
