package com.cloudmall.coupon.convert;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.api.response.UserCouponResp;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.enums.IssuedSourceEnum;

/**
 * 用户券转换器。
 * <p>
 * 实体与响应互转，以及发放时从模板快照装配用户券（状态流水见 UserCouponLogConverter）。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface UserCouponConverter {

    /**
     * 转换用户券实体为响应对象。
     *
     * @param userCoupon 用户券实体
     * @return 响应对象
     */
    UserCouponResp toResp(UserCouponDO userCoupon);

    /**
     * 批量转换用户券实体列表为响应列表。
     *
     * @param userCoupons 用户券实体列表
     * @return 响应列表
     */
    List<UserCouponResp> toRespList(List<UserCouponDO> userCoupons);

    /**
     * 发放装配：从模板快照 + 发放参数构建用户券。 <p>
     * 快照字段来自模板，createTime/updateTime/deleted 由
     * MyBatis-Plus 自动填充。
     *
     * @param template 模板
     * @param userId 用户 ID
     * @param source 发放来源
     * @param eventId 自动发放事件 ID（公开领取为 null）
     * @param now 当前时间（用于状态变更时间）
     * @param effective 实际生效时间
     * @param expire 实际过期时间
     * @return 用户券实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "stateTime", source = "now")
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "lockedOrderNo", ignore = true)
    @Mapping(target = "expireTime", source = "expire")
    @Mapping(target = "lockExpireTime", ignore = true)
    @Mapping(target = "issuedSource", source = "source")
    @Mapping(target = "couponId", source = "template.id")
    @Mapping(target = "shopId", source = "template.shopId")
    @Mapping(target = "effectiveTime", source = "effective")
    @Mapping(target = "nameSnapshot", source = "template.name")
    @Mapping(target = "typeSnapshot", source = "template.type")
    @Mapping(target = "ownerType", source = "template.ownerType")
    @Mapping(target = "scopeTypeSnapshot", source = "template.scopeType")
    @Mapping(target = "thresholdSnapshot", source = "template.threshold")
    @Mapping(target = "discountRateSnapshot", source = "template.discountRate")
    @Mapping(target = "discountAmountSnapshot", source = "template.discountAmount")
    @Mapping(
            target = "status",
            expression = "java(com.cloudmall.coupon.enums.UserCouponStatusEnum.AVAILABLE)"
    )
    @Mapping(
            target = "issuedEventId",
            expression = "java(source == com.cloudmall.coupon.enums.IssuedSourceEnum.REGISTER_AUTO ? eventId : null)"
    )
    UserCouponDO toIssue(
            CouponDO template,
            Long userId,
            IssuedSourceEnum source,
            String eventId,
            LocalDateTime now,
            LocalDateTime effective,
            LocalDateTime expire
    );

}
