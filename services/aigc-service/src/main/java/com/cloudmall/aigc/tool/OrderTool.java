package com.cloudmall.aigc.tool;

import com.cloudmall.aigc.agent.AgentTool;
import com.cloudmall.aigc.agent.AgentType;
import com.cloudmall.context.UserContextHolder;
import com.cloudmall.order.api.client.OrderClient;
import com.cloudmall.order.api.response.OrderResp;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * 订单工具，提供订单查询能力。
 */
@AgentTool(agents = AgentType.ORDER)
@RequiredArgsConstructor
public class OrderTool {

    private final OrderClient orderClient;

    /**
     * 根据订单 ID 查询订单详情
     *
     * @param id          订单 ID
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 订单详情，未找到时为 null
     */
    @Tool(description = "根据订单ID查询订单详情，返回订单编号、状态、金额、商品列表、收货地址等完整信息")
    public OrderResp getOrderById(
            @ToolParam(description = "订单ID") Long id,
            ToolContext toolContext
    ) {
        var result = orderClient.getById(id);
        if (result == null || result.getData() == null) {
            return null;
        }

        OrderResp order = result.getData();
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
    public List<OrderResp> listMyOrders(
            ToolContext toolContext
    ) {
        Long userId = UserContextHolder.getUserId();
        var result = orderClient.listByUser(userId);
        if (result == null || result.getData() == null) {
            return List.of();
        }

        List<OrderResp> orders = result.getData();
        if (toolContext.getContext().get("resultContext") instanceof ToolResultContext resultContext) {
            resultContext.put("OrderList", orders);
        }
        return orders;
    }
}
