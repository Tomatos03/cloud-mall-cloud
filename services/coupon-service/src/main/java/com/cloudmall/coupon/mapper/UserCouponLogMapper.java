package com.cloudmall.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.cloudmall.coupon.entity.UserCouponLogDO;
import com.cloudmall.coupon.enums.UserCouponOperationTypeEnum;

/**
 * 用户券状态流水 Mapper。
 *
 * @author : Tomatos
 */
@Mapper
public interface UserCouponLogMapper extends BaseMapper<UserCouponLogDO> {

    /**
     * 统计指定事件下某操作类型的流水条数（幂等判定）。
     *
     * @param eventId 事件 ID
     * @param op 操作类型
     * @return 流水条数
     */
    @Select(
            """
            SELECT COUNT(*)
            FROM cm_user_coupon_log
            WHERE event_id = #{eventId} AND operation_type = #{op}
            """
    )
    Long countByEventAndOperation(
            @Param("eventId")
            String eventId,
            @Param("op")
            UserCouponOperationTypeEnum op
    );
}
