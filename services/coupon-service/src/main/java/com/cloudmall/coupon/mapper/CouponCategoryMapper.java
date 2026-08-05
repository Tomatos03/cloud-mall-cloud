package com.cloudmall.coupon.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select(
            """
            SELECT category_id
            FROM cm_coupon_category
            WHERE coupon_id = #{couponId}
            """
    )
    List<Long> selectIdsByCouponId(
            @Param("couponId")
            Long couponId
    );

    /**
     * 统计模板配置的分类数量。
     *
     * @param couponId 模板 ID
     * @return 分类数量
     */
    @Select(
            """
            SELECT COUNT(*)
            FROM cm_coupon_category
            WHERE coupon_id = #{couponId}
            """
    )
    long countByCouponId(
            @Param("couponId")
            Long couponId
    );

    /**
     * 删除模板全部分类范围关联。
     *
     * @param couponId 模板 ID
     * @return 影响行数
     */
    @Delete(
            """
            DELETE FROM cm_coupon_category
            WHERE coupon_id = #{couponId}
            """
    )
    int deleteByCouponId(
            @Param("couponId")
            Long couponId
    );
}
