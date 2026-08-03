package com.cloudmall.coupon.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.coupon.api.response.CouponOperationLogResp;
import com.cloudmall.coupon.entity.CouponOperationLogDO;

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
}
