package com.cloudmall.user.api.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.user.api.client.UserClient;
import com.cloudmall.user.api.model.resp.UserResp;

/**
 * 用户客户端兜底工厂
 * <p>
 * 昵称头像等展示信息读取失败时快速失败，避免返回 null 引发 NPE。
 *
 * @author : Tomatos
 */
@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.warn("用户客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new UserClient() {
            @Override
            public UserResp getById(Long id) {
                throw failure;
            }
        };
    }
}
