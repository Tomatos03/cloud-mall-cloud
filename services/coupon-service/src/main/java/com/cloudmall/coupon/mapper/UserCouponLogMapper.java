package com.cloudmall.coupon.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.cloudmall.coupon.model.entity.UserCouponLogDO;
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
    default Long countByEventAndOperation(String eventId, UserCouponOperationTypeEnum op) {
        return selectCount(
                Wrappers.<UserCouponLogDO>lambdaQuery()
                        .eq(UserCouponLogDO::getEventId, eventId)
                        .eq(UserCouponLogDO::getOperationType, op)
        );
    }

    /**
     * 按模板查询用户券流水（模板为空时查询全量，供统计聚合）。
     *
     * @param couponId 模板 ID（可空，空表示全量）
     * @return 用户券流水列表
     */
    default List<UserCouponLogDO> selectByCouponId(Long couponId) {
        return selectList(
                Wrappers.<UserCouponLogDO>lambdaQuery()
                        .eq(couponId != null, UserCouponLogDO::getCouponId, couponId)
        );
    }
}
