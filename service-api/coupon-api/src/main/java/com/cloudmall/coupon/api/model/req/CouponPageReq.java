package com.cloudmall.coupon.api.model.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.cloudmall.common.model.page.PageReq;
import com.cloudmall.coupon.api.enums.CouponOwnerEnum;
import com.cloudmall.coupon.api.enums.CouponStatusEnum;
import com.cloudmall.coupon.api.enums.IssueChannelEnum;
import com.cloudmall.mybatisplus.annotation.OperatorEnum;
import com.cloudmall.mybatisplus.annotation.QueryCondition;

/**
 * 优惠券模板分页查询请求
 * <p>
 * 商家端强制注入 shopId，运营端可按 ownerType/status/issueChannel 等过滤全量。
 *
 * @author : Tomatos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponPageReq extends PageReq {

    /** 模板 ID（等值匹配） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long id;

    /** 店铺 ID（等值匹配，商家端强制注入） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private Long shopId;

    /** 模板名称（模糊匹配） */
    @QueryCondition(operator = OperatorEnum.LIKE)
    private String name;

    /** 券归属（PLATFORM/SHOP） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private CouponOwnerEnum ownerType;

    /** 模板状态 */
    @QueryCondition(operator = OperatorEnum.EQ)
    private CouponStatusEnum status;

    /** 发行渠道（PUBLIC_CLAIM / REGISTER_AUTO） */
    @QueryCondition(operator = OperatorEnum.EQ)
    private IssueChannelEnum issueChannel;

    /**
     * 使用指定参数构造对象。
     *
     * @param current 当前页码
     * @param size 页大小
     */
    public CouponPageReq(long current, long size) {
        super(current, size);
    }

}
