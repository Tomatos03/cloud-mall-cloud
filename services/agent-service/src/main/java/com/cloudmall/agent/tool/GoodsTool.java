package com.cloudmall.agent.tool;

import com.cloudmall.agent.agent.AgentTool;
import com.cloudmall.agent.agent.AgentType;
import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.client.GoodsClient;
import com.cloudmall.goods.api.response.GoodsResp;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 商品查询工具
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@AgentTool(agents = AgentType.GOODS)
@RequiredArgsConstructor
public class GoodsTool {

    private final GoodsClient goodsClient;

    /**
     * 根据商品 SPU 编号查询商品详情
     * <p>
     * 查询结果会自动放入 ToolResultContext，key 为 "GoodsResp"
     * </p>
     *
     * @param spu         商品 SPU 编号
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 商品详情对象，未找到时为 null
     */
    @Tool(description = "根据商品SPU编号查询商品详情，返回商品名称、价格、销量、库存、分类等完整信息")
    public GoodsResp getGoodsBySpu(
            @ToolParam(description = "商品SPU编号") String spu,
            ToolContext toolContext
    ) {
        Result<GoodsResp> result = goodsClient.getBySpu(spu);
        if (result == null || result.getData() == null) {
            return null;
        }

        GoodsResp good = result.getData();
        if (toolContext.getContext().get("resultContext") instanceof ToolResultContext resultContext) {
            resultContext.put(GoodsResp.class.getSimpleName(), good);
        }
        return good;
    }
}
