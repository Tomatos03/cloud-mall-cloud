package com.cloudmall.aigc.tool;

import com.cloudmall.aigc.agent.AgentTool;
import com.cloudmall.aigc.agent.AgentType;
import com.cloudmall.common.model.Result;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.response.ProductResp;
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
@AgentTool(agents = AgentType.PRODUCT)
@RequiredArgsConstructor
public class ProductTool {

    private final ProductClient productClient;

    /**
     * 根据商品 SPU 编号查询商品详情
     * <p>
     * 查询结果会自动放入 ToolResultContext，key 为 "ProductResp"
     * </p>
     *
     * @param spu         商品 SPU 编号
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 商品详情对象，未找到时为 null
     */
    @Tool(description = "根据商品SPU编号查询商品详情，返回商品名称、价格、分类、品牌、描述等完整信息")
    public ProductResp getProductBySpu(
            @ToolParam(description = "商品SPU编号") String spu,
            ToolContext toolContext
    ) {
        Result<ProductResp> result = productClient.getBySpu(spu);
        if (result == null || result.getData() == null) {
            return null;
        }

        ProductResp product = result.getData();
        if (toolContext.getContext().get("resultContext") instanceof ToolResultContext resultContext) {
            resultContext.put(ProductResp.class.getSimpleName(), product);
        }
        return product;
    }
}
