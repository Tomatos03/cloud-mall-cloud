package com.cloudmall.shared.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.shared.api.client.InternalSharedClient;
import com.cloudmall.shared.api.model.req.FileBindReq;
import com.cloudmall.shared.api.model.req.FileReleaseReq;

/**
 * 文件台账内部客户端兜底工厂
 * <p>
 * 文件绑定与释放由 Outbox 保证最终一致，这里快速失败即可，不伪造成功结果。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalSharedClientFallbackFactory implements FallbackFactory<InternalSharedClient> {

    @Override
    public InternalSharedClient create(Throwable cause) {
        log.warn("文件台账内部客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalSharedClient() {
            @Override
            public void bind(FileBindReq req) {
                throw failure;
            }

            @Override
            public void release(FileReleaseReq req) {
                throw failure;
            }
        };
    }
}
