package com.cloudmall.product.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.product.api.client.InternalProductClient;
import com.cloudmall.product.api.model.req.StockConfirmReq;
import com.cloudmall.product.api.model.req.StockOpReq;
import com.cloudmall.product.api.model.req.StockReleaseByOrderReq;

/**
 * 商品内部客户端兜底工厂
 * <p>
 * 库存预占、确认、释放与补偿必须快速失败，空值降级会造成超卖或少卖。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalProductClientFallbackFactory implements FallbackFactory<InternalProductClient> {

    @Override
    public InternalProductClient create(Throwable cause) {
        log.warn("商品内部客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalProductClient() {
            @Override
            public void reserve(StockOpReq req) {
                throw failure;
            }

            @Override
            public void confirm(StockConfirmReq req) {
                throw failure;
            }

            @Override
            public void release(StockOpReq req) {
                throw failure;
            }

            @Override
            public void compensate(StockOpReq req) {
                throw failure;
            }

            @Override
            public void releaseByOrder(StockReleaseByOrderReq req) {
                throw failure;
            }
        };
    }
}
