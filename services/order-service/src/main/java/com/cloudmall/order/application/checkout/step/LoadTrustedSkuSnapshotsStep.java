package com.cloudmall.order.application.checkout.step;

import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollStreamUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.product.api.client.SkuClient;
import com.cloudmall.product.api.model.resp.SkuResp;

/**
 * 加载可信 SKU 快照并校验库存步骤：按结算项加载 SKU 快照并校验存在与库存，
 * 通过后写入 context.skuMap。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class LoadTrustedSkuSnapshotsStep implements ICheckoutStep {

    private final SkuClient skuClient;

    /**
     * 步骤名称。
     *
     * @return 加载可信 SKU 快照并校验库存
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.LOAD_TRUSTED_SKU_SNAPSHOTS;
    }

    /**
     * 执行步骤。
     *
     * @param context 订单上下文
     */
    @Override
    public void execute(CheckoutContext context) {
        Map<Long, Integer> checkoutItems = context.getCheckoutItems();
        List<Long> selectedSkuList = checkoutItems.keySet()
                                                  .stream()
                                                  .toList();
        List<SkuResp> skus = skuClient.listSkusByIds(selectedSkuList);
        Asserts.isTrue(skus.size() == selectedSkuList.size(), BizErrorEnum.INVALID_PARAM);

        Map<Long, SkuResp> skuMap = CollStreamUtil.toIdentityMap(skus, SkuResp::getSkuId);
        for (Map.Entry<Long, Integer> entry : checkoutItems.entrySet()) {
            Integer stock = skuMap.get(entry.getKey()).getStock();
            Asserts.greaterThan(stock, entry.getValue(), BizErrorEnum.STOCK_NOT_ENOUGH);
        }
        context.setSkuMap(skuMap);
    }
}
