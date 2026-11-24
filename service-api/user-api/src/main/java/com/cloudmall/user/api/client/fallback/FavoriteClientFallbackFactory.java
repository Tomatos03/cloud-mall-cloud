package com.cloudmall.user.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.user.api.client.FavoriteClient;

/**
 * 收藏客户端兜底工厂
 * <p>
 * 收藏/取消收藏必须快速失败，避免用户误判操作结果。
 *
 * @author : Tomatos
 */
@Slf4j
public class FavoriteClientFallbackFactory implements FallbackFactory<FavoriteClient> {

    @Override
    public FavoriteClient create(Throwable cause) {
        log.warn("收藏客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new FavoriteClient() {
            @Override
            public Void favorite(Long productId) {
                throw failure;
            }

            @Override
            public Void unfavorite(Long productId) {
                throw failure;
            }

            @Override
            public List<Long> listProductIds() {
                throw failure;
            }
        };
    }
}
