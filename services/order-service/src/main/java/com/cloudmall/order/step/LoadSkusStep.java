package com.cloudmall.order.step;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.CheckoutContext;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.response.SkuResp;

/**
 * SKU 加载与校验步骤：按结算项加载 SKU 快照并校验存在与库存，
 * 通过后写入 context.skuMap。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class LoadSkusStep implements CheckoutStep {

    private final SkuClient skuClient;

    /**
     * 步骤名称。
     *
     * @return SKU 加载与校验
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.LOAD_SKUS;
    }

    /**
     * 执行步骤。
     *
     * @param ctx 订单上下文
     */
    @Override
    public void process(CheckoutContext ctx) {
        Map<Long, Integer> checkoutItems = ctx.getCheckoutItems();
        List<Long> selectedSkuList = checkoutItems.keySet()
                                                  .stream()
                                                  .toList();
        List<SkuResp> skus = skuClient.listSkusByIds(selectedSkuList);
        Asserts.isTrue(skus.size() == selectedSkuList.size(), BizError.INVALID_PARAM);

        Map<Long, SkuResp> skuMap = skus.stream()
                                        .collect(Collectors.toMap(SkuResp::getSkuId, Function.identity()));
        for (Map.Entry<Long, Integer> entry : checkoutItems.entrySet()) {
            Integer stock = skuMap.get(entry.getKey()).getStock();
            Asserts.greaterThan(stock, entry.getValue(), BizError.STOCK_NOT_ENOUGH);
        }
        ctx.setSkuMap(skuMap);
    }
}
