package com.cloudmall.coupon.convert;

import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.entity.UserCouponLogDO;
import com.cloudmall.coupon.enums.UserCouponLogSourceEnum;
import com.cloudmall.coupon.enums.UserCouponOperationTypeEnum;
import com.cloudmall.coupon.enums.UserCouponStatusEnum;

/**
 * 用户券状态流水转换器。
 * <p>
 * 负责 UserCouponLogDO 的装配，createTime 由 MyBatis-Plus 自动填充。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface UserCouponLogConverter {

    /**
     * 状态流水装配：从用户券 + 操作上下文构建流水。
     *
     * @param uc 用户券
     * @param operationType 操作类型
     * @param fromStatus 原状态
     * @param toStatus 新状态
     * @param source 来源
     * @param orderNo 订单号
     * @param eventId 事件 ID
     * @param operatorId 操作人 ID
     * @param reason 原因
     * @return 流水实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "userId", source = "uc.userId")
    @Mapping(target = "userCouponId", source = "uc.id")
    @Mapping(target = "couponId", source = "uc.couponId")
    UserCouponLogDO toLog(
            UserCouponDO uc,
            UserCouponOperationTypeEnum operationType,
            UserCouponStatusEnum fromStatus,
            UserCouponStatusEnum toStatus,
            UserCouponLogSourceEnum source,
            String orderNo,
            String eventId,
            Long operatorId,
            String reason
    );

    /**
     * 过期流水装配：AVAILABLE → EXPIRED 的系统自动过期。
     *
     * @param uc 用户券
     * @param reason 过期原因
     * @return 流水实体
     */
    default UserCouponLogDO toExpireLog(UserCouponDO uc, String reason) {
        return toLog(
                uc,
                UserCouponOperationTypeEnum.EXPIRE,
                UserCouponStatusEnum.AVAILABLE,
                UserCouponStatusEnum.EXPIRED,
                UserCouponLogSourceEnum.SYSTEM,
                null,
                null,
                null,
                reason
        );
    }

    /**
     * 批量撤销流水装配：AVAILABLE → REVOKED 的运营撤销。
     *
     * @param ucs 用户券列表
     * @param operatorId 操作人 ID
     * @param reason 撤销原因（为空时使用默认原因）
     * @return 流水实体列表
     */
    default List<UserCouponLogDO> toRevokeLogList(List<UserCouponDO> ucs, Long operatorId, String reason) {
        return ucs
                .stream()
                .map(
                        uc -> toLog(
                                uc,
                                UserCouponOperationTypeEnum.REVOKE,
                                UserCouponStatusEnum.AVAILABLE,
                                UserCouponStatusEnum.REVOKED,
                                UserCouponLogSourceEnum.OPERATOR,
                                null,
                                null,
                                operatorId,
                                ObjectUtil.defaultIfNull(reason, "运营紧急撤销")
                        )
                )
                .toList();
    }

    /**
     * 批量过期流水装配：AVAILABLE → EXPIRED 的系统自动过期。
     *
     * @param ucs 用户券列表
     * @param reason 过期原因
     * @return 流水实体列表
     */
    default List<UserCouponLogDO> toExpireLogList(List<UserCouponDO> ucs, String reason) {
        return ucs.stream()
                  .map(uc -> toExpireLog(uc, reason))
                  .toList();
    }
}
