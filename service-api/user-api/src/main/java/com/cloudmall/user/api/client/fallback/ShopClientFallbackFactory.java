package com.cloudmall.user.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.shared.api.model.resp.ShopInfoResp;
import com.cloudmall.user.api.client.ShopClient;
import com.cloudmall.user.api.model.resp.ShopResp;

/**
 * 店铺客户端兜底工厂
 * <p>
 * 仅商品详情页店铺信息块（getShopInfo）可降级为空值；该 Client 的其它方法用于商家身份与店铺存在性校验，
 * 必须快速失败：统一通过 {@link FallbackFailures} 保留远程错误码或转换为对应的业务异常。
 *
 * @author : Tomatos
 */
@Slf4j
public class ShopClientFallbackFactory implements FallbackFactory<ShopClient> {

    @Override
    public ShopClient create(Throwable cause) {
        return new ShopClient() {

            @Override
            public ShopInfoResp getShopInfo(Long id) {
                log.warn("商品详情页店铺信息获取失败，降级为空，shopId={}", id, cause);
                return null;
            }

            @Override
            public ShopResp getByUserId(Long userId) {
                throw FallbackFailures.of(cause, BizErrorEnum.MERCHANT_STORE_NOT_FOUND);
            }

            @Override
            public ShopResp getById(Long id) {
                throw FallbackFailures.of(cause, BizErrorEnum.STORE_NOT_EXIST);
            }

            @Override
            public ShopResp getCurrent() {
                throw FallbackFailures.of(cause, BizErrorEnum.MERCHANT_STORE_NOT_FOUND);
            }

            @Override
            public List<ShopResp> listByIds(List<Long> shopIds) {
                throw FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);
            }
        };
    }

}
