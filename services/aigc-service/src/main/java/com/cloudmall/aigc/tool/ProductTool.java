package com.cloudmall.aigc.tool;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.cloudmall.aigc.agent.AgentTool;
import com.cloudmall.aigc.agent.AgentTypeEnum;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.model.resp.ProductResp;
import com.cloudmall.search.api.client.SearchClient;
import com.cloudmall.search.api.model.req.SearchProductReq;
import com.cloudmall.search.api.model.resp.SearchProductResp;
import com.cloudmall.search.api.model.resp.SearchResultResp;

/**
 * 商品查询工具
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@RequiredArgsConstructor
@AgentTool(agents = AgentTypeEnum.PRODUCT)
public class ProductTool {

    private final ProductClient productClient;
    private final SearchClient searchClient;
    private final AgentCardAssembler cardAssembler;

    /**
     * 根据商品 SPU 编号查询商品详情 <p>
     * 查询结果会转换为前端商品卡片并放入 ToolResultContext。 </p>
     *
     * @param spu 商品 SPU 编号
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 商品详情对象，未找到时为 null
     */
    @Tool(description = "根据商品SPU编号查询商品详情，返回商品名称、价格、分类、品牌、描述等完整信息")
    public ProductResp getProductBySpu(
        @ToolParam(description = "商品SPU编号")
        String spu,
        ToolContext toolContext
    ) {
        ProductResp product = productClient.getBySpu(spu);
        if (product == null) {
            return null;
        }
        if (toolContext.getContext().get(ToolContextKeys.RESULT_CONTEXT) instanceof ToolResultContext resultContext) {
            resultContext.addCards(List.of(cardAssembler.product(product)));
        }
        return product;
    }

    /**
     * 根据关键词搜索商品。
     *
     * @param keyword 搜索关键词
     * @param limit 返回数量，默认 5，最大 10
     * @param toolContext 工具上下文，用于存储查询结果
     * @return 商品搜索结果
     */
    @Tool(description = "根据关键词搜索商品，返回商品名称、价格、品牌、分类、销量和商品ID，适合按名称/品类/品牌查找商品")
    public List<SearchProductResp> searchProducts(
        @ToolParam(description = "搜索关键词，如商品名称、品牌或品类")
        String keyword,
        @ToolParam(description = "返回数量，默认5，最多10")
        Integer limit,
        ToolContext toolContext
    ) {
        if (StrUtil.isBlank(keyword)) {
            return List.of();
        }
        SearchProductReq req = SearchProductReq.builder()
                                               .keyword(keyword)
                                               .page(1)
                                               .pageSize(normalizeLimit(limit))
                                               .build();
        SearchResultResp result = searchClient.search(req);
        if (result == null || CollUtil.isEmpty(result.getItems())) {
            return List.of();
        }
        List<SearchProductResp> products = result.getItems();
        if (toolContext.getContext().get(ToolContextKeys.RESULT_CONTEXT) instanceof ToolResultContext resultContext) {
            resultContext.addCards(cardAssembler.products(products));
        }
        return products;
    }

    private int normalizeLimit(Integer limit) {
        int size = ObjectUtil.defaultIfNull(limit, 5);
        return Math.min(Math.max(size, 1), 10);
    }
}
