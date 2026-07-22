package com.cloudmall.agent.tool;

import com.cloudmall.common.entity.Result;
import com.cloudmall.goods.api.client.GoodsClient;
import com.cloudmall.goods.api.response.GoodsResp;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 商品查询工具
 *
 * @author : Tomatos
 * @date : 2026/7/22
 */
@Component
@RequiredArgsConstructor
public class GoodsTool implements AgentTool {

    private final GoodsClient goodsClient;

    /**
     * 根据商品 SPU 编号查询商品详情
     *
     * @param spu 商品 SPU 编号
     * @return 商品详情对象，未找到时为 null
     */
    @Tool(description = "根据商品SPU编号查询商品详情，返回商品名称、价格、销量、库存、分类等完整信息")
    public GoodsResp getGoodsBySpu(@ToolParam(description = "商品SPU编号") String spu) {
        return Optional.ofNullable(goodsClient.getBySpu(spu))
                       .map(Result::getData)
                       .orElse(null);
    }
}
