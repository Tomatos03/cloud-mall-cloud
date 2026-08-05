package com.cloudmall.coupon.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.model.req.CouponPageReq;

/**
 * 优惠券模板 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 分页查询优惠券模板，按创建时间倒序。 <p>
     * 条件由 {@link CouponPageReq} 的
     * {@code @QueryCondition} 注解声明。
     *
     * @param req 分页 + 筛选条件
     * @return 模板分页数据
     */
    default IPage<CouponDO> selectCouponPage(CouponPageReq req) {
        return selectPage(req, req.buildLambdaWrapper());
    }

    /**
     * 查询注册自动发放的可用平台模板。
     *
     * @param now 当前时间
     * @return 模板列表
     */
    @Select(
            """
            SELECT *
            FROM cm_coupon
            WHERE status = 'ACTIVE'
              AND owner_type = 'PLATFORM'
              AND issue_channel = 'REGISTER_AUTO'
              AND issue_start_time <= #{now}
              AND issue_end_time >= #{now}
              AND deleted = 0
            """
    )
    List<CouponDO> selectRegisterAutoTemplates(
            @Param("now")
            LocalDateTime now
    );

    /**
     * 查询公开券中心全部模板，按 ID 升序。
     *
     * @return 模板列表
     */
    @Select(
            """
            SELECT *
            FROM cm_coupon
            WHERE issue_channel = 'PUBLIC_CLAIM'
            ORDER BY id
            """
    )
    List<CouponDO> selectPublicClaimTemplates();

    /**
     * 原子递增发行数量（并发领取专用）。 <p>
     * 仅当模板处于 ACTIVE、允许公开领取、处于发放窗口内且 有限库存未耗尽时递增，并顺带落库首次发放
     * 时间。
     *
     * @param couponId 模板 ID
     * @return 影响行数，0 表示模板不可领取或库存不足
     */
    @Update(
            """
            UPDATE cm_coupon
            SET claimed_count = claimed_count + 1,
                first_issued_time = COALESCE(first_issued_time, NOW()),
                update_time = NOW()
            WHERE id = #{couponId}
              AND deleted = 0
              AND status = 'ACTIVE'
              AND issue_channel = 'PUBLIC_CLAIM'
              AND issue_start_time <= NOW()
              AND issue_end_time >= NOW()
              AND (total_count IS NULL OR claimed_count < total_count)
            """
    )
    int increaseClaimedCount(
            @Param("couponId")
            Long couponId
    );
}
