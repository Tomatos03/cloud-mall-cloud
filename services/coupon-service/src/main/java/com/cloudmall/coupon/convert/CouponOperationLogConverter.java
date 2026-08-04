package com.cloudmall.coupon.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.api.response.CouponOperationLogResp;
import com.cloudmall.coupon.entity.CouponDO;
import com.cloudmall.coupon.entity.CouponOperationLogDO;
import com.cloudmall.coupon.enums.CouponOperationActionEnum;
import com.cloudmall.coupon.enums.CouponStatusEnum;

/**
 * 模板操作流水转换器
 * <p>
 * 枚举状态映射为 code 字符串。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface CouponOperationLogConverter {

    /**
     * 操作流水实体转响应。
     *
     * @param log 操作流水实体
     * @return 操作流水响应
     */
    @Mapping(target = "toStatus", source = "toStatus.code")
    @Mapping(target = "actionType", source = "actionType.code")
    @Mapping(target = "fromStatus", source = "fromStatus.code")
    @Mapping(target = "operatorType", source = "operatorType.code")
    CouponOperationLogResp toResp(CouponOperationLogDO log);

    /**
     * 批量转换操作流水实体列表为响应列表。
     *
     * @param logs 操作流水实体列表
     * @return 操作流水响应列表
     */
    List<CouponOperationLogResp> toRespList(List<CouponOperationLogDO> logs);

    /**
     * 组装模板操作流水实体：操作人与操作类型取自当前用户上下文。
     *
     * @param coupon 模板
     * @param from 操作前状态
     * @param to 操作后状态
     * @param action 操作类型
     * @param summary 变更摘要
     * @param reason 原因
     * @return 操作流水实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "couponId", source = "coupon.id")
    @Mapping(target = "shopId", source = "coupon.shopId")
    @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(
            target = "operatorId",
            expression = "java(com.cloudmall.common.context.UserContextHolder.getUserId())"
    )
    @Mapping(
            target = "operatorType",
            expression = "java(com.cloudmall.common.context.UserContextHolder.isMerchant() ? com.cloudmall.coupon.enums.CouponOperatorTypeEnum.MERCHANT : com.cloudmall.coupon.enums.CouponOperatorTypeEnum.OPERATOR)"
    )
    CouponOperationLogDO toLog(
            CouponDO coupon,
            CouponStatusEnum from,
            CouponStatusEnum to,
            CouponOperationActionEnum action,
            String summary,
            String reason
    );
}
