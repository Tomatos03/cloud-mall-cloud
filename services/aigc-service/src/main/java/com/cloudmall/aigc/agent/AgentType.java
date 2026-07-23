package com.cloudmall.aigc.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 智能体类型枚举。
 * <p>
 * 每个枚举常量携带默认描述，用于路由 LLM 展示各智能体的职责范围。
 * 描述内容可根据业务需求调整。（配置文件中每个智能体都有独立的提示词）
 */
@Getter
@AllArgsConstructor
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_VALUES)
public enum AgentType {
    GOODS("负责商品搜索、详情查询、价格库存查看、分类浏览和商品推荐"),
    ORDER("负责订单查询、下单、物流跟踪、购物车管理"),
    COUPON("负责优惠券领取、使用查询、过期提醒"),
    USER("负责账户信息、地址管理、登录注册等用户服务"),
    GENERAL("负责闲聊、问候及其他非业务问题的处理"),
    ROUTE("分析用户意图并路由到合适的业务智能体");

    private final String description;
}
