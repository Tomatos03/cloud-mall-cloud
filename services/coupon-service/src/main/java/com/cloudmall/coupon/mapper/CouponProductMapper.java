package com.cloudmall.coupon.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cloudmall.coupon.entity.CouponProductDO;

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
    @Select(
            """
            SELECT product_id
            FROM cm_coupon_product
            WHERE coupon_id = #{couponId}
            """
    )
    List<Long> selectIdsByCouponId(
            @Param("couponId")
            Long couponId
    );

    /**
     * 统计模板配置的商品数量。
     *
     * @param couponId 模板 ID
     * @return 商品数量
     */
    @Select(
            """
            SELECT COUNT(*)
            FROM cm_coupon_product
            WHERE coupon_id = #{couponId}
            """
    )
    long countByCouponId(
            @Param("couponId")
            Long couponId
    );

    /**
     * 删除模板全部商品范围关联。
     *
     * @param couponId 模板 ID
     * @return 影响行数
     */
    @Delete(
            """
            DELETE FROM cm_coupon_product
            WHERE coupon_id = #{couponId}
            """
    )
    int deleteByCouponId(
            @Param("couponId")
            Long couponId
    );
}
