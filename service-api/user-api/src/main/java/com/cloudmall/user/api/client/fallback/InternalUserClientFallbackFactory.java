package com.cloudmall.user.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.user.api.client.InternalUserClient;
import com.cloudmall.user.api.model.req.OrderPointAwardReq;
import com.cloudmall.user.api.model.resp.AddressResp;
import com.cloudmall.user.api.model.resp.UserResp;

/**
 * 用户内部客户端兜底工厂
 * <p>
 * 地址校验、积分任务发放等均为订单协同操作，失败必须快速失败。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalUserClientFallbackFactory implements FallbackFactory<InternalUserClient> {

    @Override
    public InternalUserClient create(Throwable cause) {
        log.warn("用户内部客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalUserClient() {
            @Override
            public List<UserResp> listByIds(List<Long> ids) {
                throw failure;
            }

            @Override
            public AddressResp getAddressById(Long id, Long userId) {
                throw failure;
            }

            @Override
            public void completePointTask(String code, Long userId) {
                throw failure;
            }

            @Override
            public void awardOrderPoints(OrderPointAwardReq req) {
                throw failure;
            }
        };
    }
}
