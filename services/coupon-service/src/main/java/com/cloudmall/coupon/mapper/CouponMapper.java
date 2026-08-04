package com.cloudmall.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.cloudmall.coupon.entity.CouponDO;

/**
 * 优惠券模板 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 原子递增发行数量（并发领取专用）。 <p>
     * 仅当模板处于 ACTIVE、允许公开领取、处于发放窗口内且 有限库存未耗尽时递增，并顺带落库首次发放
     * 时间。
     *
     * @param couponId 模板 ID
     * @return 影响行数，0 表示模板不可领取或库存不足
     */
    @Update(
            "UPDATE cm_coupon SET claimed_count = claimed_count + 1, "
                    + "first_issued_time = COALESCE(first_issued_time, NOW()), update_time = NOW() "
                    + "WHERE id = #{couponId} AND deleted = 0 AND status = 'ACTIVE' "
                    + "AND issue_channel = 'PUBLIC_CLAIM' AND issue_start_time <= NOW() AND issue_end_time >= NOW() "
                    + "AND (total_count IS NULL OR claimed_count < total_count)"
    )
    int increaseClaimedCount(
            @Param("couponId")
            Long couponId
    );
}
