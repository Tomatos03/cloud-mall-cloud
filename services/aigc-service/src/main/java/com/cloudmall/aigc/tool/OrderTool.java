package com.cloudmall.aigc.tool;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.cloudmall.aigc.agent.AgentTool;
import com.cloudmall.aigc.agent.AgentTypeEnum;
import com.cloudmall.common.context.UserContextHolder;
import com.cloudmall.order.api.client.OrderClient;
import com.cloudmall.order.api.model.resp.OrderResp;

/**
 * 订单工具，提供订单查询能力。
 */
@RequiredArgsConstructor
@AgentTool(agents = AgentTypeEnum.ORDER)
public class OrderTool {

    private final OrderClient orderClient;

    /**
     * 根据订单 ID 查询订单详情
     *
     * @param id 订单 ID
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 订单详情，未找到时为 null
     */
    public OrderResp getOrderById(@ToolParam(description = "订单ID") Long id, ToolContext toolContext) {
        OrderResp order = orderClient.getById(id);
        if (order == null) {
            return null;
        }
        if (toolContext.getContext().get("resultContext") instanceof ToolResultContext resultContext) {
            resultContext.put(OrderResp.class.getSimpleName(), order);
        }
        return order;
    }

    /**
     * 查询当前用户的订单列表
     *
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 当前用户的订单列表
     */
    @Tool(description = "查询当前用户的订单列表，返回所有订单的概要信息（订单号、状态、金额、下单时间）")
    public List<OrderResp> listMyOrders(ToolContext toolContext) {
        Long userId = UserContextHolder.getUserId();
        Page<OrderResp> page = orderClient.page(userId, 1, 100);
        if (page == null || CollUtil.isEmpty(page.getRecords())) {
            return List.of();
        }
        List<OrderResp> orders = page.getRecords();
        if (toolContext.getContext().get("resultContext") instanceof ToolResultContext resultContext) {
            resultContext.put("OrderList", orders);
        }
        return orders;
    }
}
