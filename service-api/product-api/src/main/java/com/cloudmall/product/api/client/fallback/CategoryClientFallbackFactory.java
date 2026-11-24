package com.cloudmall.product.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.product.api.client.CategoryClient;
import com.cloudmall.product.api.model.resp.CategoryResp;

/**
 * 分类客户端兜底工厂
 * <p>
 * 分类查询用于优惠券范围与商品校验，快速失败避免误判。
 *
 * @author : Tomatos
 */
@Slf4j
public class CategoryClientFallbackFactory implements FallbackFactory<CategoryClient> {

    @Override
    public CategoryClient create(Throwable cause) {
        log.warn("分类客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new CategoryClient() {
            @Override
            public CategoryResp getById(Long id) {
                throw failure;
            }

            @Override
            public List<CategoryResp> getByIds(List<Long> ids) {
                throw failure;
            }
        };
    }
}
