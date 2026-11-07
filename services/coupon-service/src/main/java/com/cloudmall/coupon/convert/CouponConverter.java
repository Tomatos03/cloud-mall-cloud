package com.cloudmall.coupon.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.cloudmall.coupon.api.model.req.CouponReq;
import com.cloudmall.coupon.api.model.resp.CouponResp;
import com.cloudmall.coupon.model.entity.CouponDO;

/**
 * 优惠券模板转换器。
 * <p>
 * claimed/claimable/stockRemaining/productIds/categoryIds 由服务层按上下文补充。
 * 创建与编辑的业务字段装配收敛在 toDraft / applyUpdate，系统字段（归属、状态、时间戳、软删标记）由服务层赋值。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface CouponConverter {

    /**
     * 转换模板实体为响应对象。
     *
     * @param coupon 模板实体
     * @return 响应对象
     */
    CouponResp toResp(CouponDO coupon);

    /**
     * 批量转换模板实体列表为响应列表。
     *
     * @param coupons 模板实体列表
     * @return 响应列表
     */
    List<CouponResp> toRespList(List<CouponDO> coupons);

    /**
     * 创建请求装配为草稿实体：业务字段搬运 + 系统字段默认值（店铺、创建人、状态、时间戳、软删标记）。
     *
     * @param req 创建请求
     * @return 草稿实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstIssuedTime", ignore = true)
    @Mapping(target = "deleted", expression = "java(false)")
    @Mapping(target = "claimedCount", expression = "java(0L)")
    @Mapping(target = "createTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", expression = "java(com.cloudmall.coupon.api.enums.CouponStatusEnum.DRAFT)")
    @Mapping(
            target = "shopId",
            expression = "java(com.cloudmall.common.context.UserContextHolder.getShopId())"
    )
    @Mapping(
            target = "createdBy",
            expression = "java(com.cloudmall.common.context.UserContextHolder.getUserId())"
    )
    @Mapping(
            target = "threshold",
            expression = "java(req.getThreshold() == null ? java.math.BigDecimal.ZERO : req.getThreshold())"
    )
    CouponDO toDraft(CouponReq req);

    /**
     * 编辑请求覆盖到已存在草稿：仅覆盖业务字段，归属、状态、时间戳等系统字段保持不变。
     *
     * @param coupon 已存在草稿（原地修改）
     * @param req 编辑请求
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shopId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "ownerType", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "claimedCount", ignore = true)
    @Mapping(target = "firstIssuedTime", ignore = true)
    @Mapping(
            target = "threshold",
            expression = "java(req.getThreshold() == null ? java.math.BigDecimal.ZERO : req.getThreshold())"
    )
    void applyUpdate(@MappingTarget CouponDO coupon, CouponReq req);
}
