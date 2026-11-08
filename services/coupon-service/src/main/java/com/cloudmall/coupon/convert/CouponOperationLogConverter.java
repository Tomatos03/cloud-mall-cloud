package com.cloudmall.coupon.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.api.enums.CouponOperationActionEnum;
import com.cloudmall.coupon.api.enums.CouponStatusEnum;
import com.cloudmall.coupon.api.model.resp.CouponOperationLogResp;
import com.cloudmall.coupon.model.entity.CouponDO;
import com.cloudmall.coupon.model.entity.CouponOperationLogDO;

/**
 * 模板操作流水转换器
 * <p>
 * 响应中的枚举字段直接使用枚举类型，JSON 序列化由枚举的 @JsonValue 输出 code。
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
    @Mapping(target = "action", source = "actionType")
    @Mapping(target = "operator", source = "operatorType")
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
    @Mapping(target = "actionType", source = "action")
    @Mapping(target = "couponId", source = "coupon.id")
    @Mapping(target = "shopId", source = "coupon.shopId")
    @Mapping(target = "changeSummary", source = "summary")
    @Mapping(
            target = "operatorId",
            expression = "java(com.cloudmall.common.context.UserContextHolder.getUserId())"
    )
    @Mapping(
            target = "operatorType",
            expression = "java(com.cloudmall.common.context.UserContextHolder.isMerchant() ? com.cloudmall.coupon.api.enums.CouponOperatorEnum.MERCHANT : com.cloudmall.coupon.api.enums.CouponOperatorEnum.OPERATOR)"
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
