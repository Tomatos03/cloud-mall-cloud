package com.cloudmall.product.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.SpringQueryMap;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.common.model.cursor.CursorReq;
import com.cloudmall.common.model.cursor.CursorResp;
import com.cloudmall.product.api.client.ProductClient;
import com.cloudmall.product.api.model.resp.ProductDetailResp;
import com.cloudmall.product.api.model.resp.ProductDocResp;
import com.cloudmall.product.api.model.resp.ProductResp;

/**
 * 商品客户端兜底工厂
 * <p>
 * 商品资料用于校验与展示，但空对象会掩盖远端故障，这里统一快速失败。
 *
 * @author : Tomatos
 */
@Slf4j
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        log.warn("商品客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new ProductClient() {
            @Override
            public ProductResp getById(Long id) {
                throw failure;
            }

            @Override
            public ProductDetailResp detail(Long id) {
                throw failure;
            }

            @Override
            public List<ProductResp> listByIds(List<Long> ids) {
                throw failure;
            }

            @Override
            public ProductResp getBySpu(String spu) {
                throw failure;
            }

            @Override
            public CursorResp<ProductDocResp> cursorPage(@SpringQueryMap CursorReq req) {
                throw failure;
            }

            @Override
            public ProductDocResp searchDoc(Long id) {
                throw failure;
            }
        };
    }
}
