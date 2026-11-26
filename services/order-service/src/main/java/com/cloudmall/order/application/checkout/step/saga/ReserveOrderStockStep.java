package com.cloudmall.order.application.checkout.step.saga;

import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.BooleanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.order.model.entity.OrderDO;
import com.cloudmall.product.api.client.InternalProductClient;
import com.cloudmall.product.api.model.req.StockOpReq;
import com.cloudmall.product.api.model.req.StockReserveItem;

/**
 * 预占订单库存子步骤
 * <p>
 * 调用商品服务预占库存，成功后在上下文打标供逆序补偿释放；释放接口按订单号幂等。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class ReserveOrderStockStep implements ISagaSteps {

    /** 下单预占库存原因 */
    private static final String RESERVE_REASON = "下单预占库存";

    /** 补偿释放原因 */
    private static final String COMPENSATE_REASON = "下单失败补偿释放";

    private final InternalProductClient internalProductClient;

    @Override
    public CheckoutSagaStepsEnum name() {
        return CheckoutSagaStepsEnum.RESERVE_ORDER_STOCK;
    }

    @Override
    public void execute(CheckoutContext context) {
        internalProductClient.reserve(buildStockOpReq(context, RESERVE_REASON));
        context.setStockReserved(true);
    }

    @Override
    public void compensate(CheckoutContext context) {
        if (!BooleanUtil.isTrue(context.getStockReserved())) {
            return;
        }
        internalProductClient.release(buildStockOpReq(context, COMPENSATE_REASON));
    }

    /**
     * 组装库存操作请求。
     *
     * @param context 订单上下文
     * @param reason 操作原因
     * @return 库存操作请求
     */
    private static StockOpReq buildStockOpReq(CheckoutContext context, String reason) {
        OrderDO order = context.getOrder();
        Map<Long, Integer> quantities = context.getCheckoutItems();
        List<StockReserveItem> items = CollStreamUtil.toList(
                quantities.entrySet(),
                entry -> StockReserveItem.builder()
                                         .skuId(entry.getKey())
                                         .quantity(entry.getValue())
                                         .build()
        );
        return StockOpReq.builder()
                         .orderNo(order.getOrderNo())
                         .items(items)
                         .reason(reason)
                         .build();
    }
}
