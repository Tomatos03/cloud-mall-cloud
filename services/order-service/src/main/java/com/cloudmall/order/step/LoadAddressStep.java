package com.cloudmall.order.step;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizError;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.CheckoutContext;
import com.cloudmall.user.api.client.InternalUserClient;
import com.cloudmall.user.api.response.AddressResp;

/**
 * 收货地址加载步骤：地址存在且归属当前用户，通过后写入 context.address。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class LoadAddressStep implements CheckoutStep {

    private final InternalUserClient internalUserClient;

    /**
     * 步骤名称。
     *
     * @return 收货地址加载
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.LOAD_ADDRESS;
    }

    /**
     * 执行步骤。
     *
     * @param ctx 订单上下文
     */
    @Override
    public void process(CheckoutContext ctx) {
        AddressResp address = internalUserClient.getAddressById(ctx.getReq().getAddressId(), ctx.getUserId());
        Asserts.notNull(address, BizError.ORDER_CREATE_ADDRESS_NOT_EXIST);
        ctx.setAddress(address);
    }
}
