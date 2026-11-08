package com.cloudmall.aigc.tool;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.cloudmall.aigc.agent.AgentTool;
import com.cloudmall.aigc.agent.AgentTypeEnum;
import com.cloudmall.coupon.api.client.CouponClient;
import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.model.resp.CouponResp;
import com.cloudmall.coupon.api.model.resp.UserCouponResp;

/**
 * 优惠券工具，提供公开券查询和我的优惠券查询能力。
 */
@RequiredArgsConstructor
@AgentTool(agents = AgentTypeEnum.COUPON)
public class CouponTool {

    private final CouponClient couponClient;
    private final InternalCouponClient internalCouponClient;
    private final AgentCardAssembler cardAssembler;

    private final int cardLimit = 5;

    /**
     * 查询当前可领取的公开优惠券。
     *
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 可领取的优惠券列表
     */
    @Tool(description = "查询当前可领取的公开优惠券，返回券名称、面额、使用门槛和有效期")
    public List<CouponResp> listAvailableCoupons(ToolContext toolContext) {
        List<CouponResp> coupons = couponClient.listPublic();
        if (CollUtil.isEmpty(coupons)) {
            return List.of();
        }
        List<CouponResp> available = coupons.stream()
                                            .filter(coupon -> Boolean.TRUE.equals(coupon.getClaimable()))
                                            .limit(cardLimit)
                                            .toList();
        if (CollUtil.isEmpty(available)) {
            return List.of();
        }
        if (toolContext.getContext().get(ToolContextKeys.RESULT_CONTEXT) instanceof ToolResultContext resultContext) {
            resultContext.addCards(cardAssembler.coupons(available));
        }
        return available;
    }

    /**
     * 查询当前用户已领取的优惠券。
     *
     * @param status 券状态，可为空
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 用户优惠券列表
     */
    @Tool(description = "查询当前用户已领取的优惠券，可按状态筛选：AVAILABLE 可使用、USED 已使用、EXPIRED 已过期")
    public List<UserCouponResp> listMyCoupons(
        @ToolParam(description = "券状态，可选值：AVAILABLE、USED、EXPIRED；为空查询全部")
        String status,
        ToolContext toolContext
    ) {
        String normalizedStatus = normalizeStatus(status);
        List<UserCouponResp> coupons = internalCouponClient.listUserCoupons(1, cardLimit, normalizedStatus);
        if (CollUtil.isEmpty(coupons)) {
            return List.of();
        }
        if (toolContext.getContext().get(ToolContextKeys.RESULT_CONTEXT) instanceof ToolResultContext resultContext) {
            resultContext.addCards(cardAssembler.userCoupons(coupons));
        }
        return coupons;
    }

    private String normalizeStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "AVAILABLE", "LOCKED", "USED", "EXPIRED", "REVOKED" -> normalized;
            default -> null;
        };
    }
}
