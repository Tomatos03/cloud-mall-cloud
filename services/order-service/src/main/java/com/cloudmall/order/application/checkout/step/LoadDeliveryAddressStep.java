package com.cloudmall.order.application.checkout.step;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.order.model.bo.CheckoutContext;
import com.cloudmall.user.api.client.InternalUserClient;
import com.cloudmall.user.api.model.resp.AddressResp;

/**
 * 加载收货地址步骤：地址存在且归属当前用户，通过后写入 context.address。
 *
 * @author : Tomatos
 */
@Component
@RequiredArgsConstructor
public class LoadDeliveryAddressStep implements ICheckoutStep {

    private final InternalUserClient internalUserClient;

    /**
     * 步骤名称。
     *
     * @return 加载收货地址
     */
    @Override
    public CheckoutStepEnum name() {
        return CheckoutStepEnum.LOAD_DELIVERY_ADDRESS;
    }

    /**
     * 执行步骤。
     *
     * @param context 订单上下文
     */
    @Override
    public void execute(CheckoutContext context) {
        AddressResp address = internalUserClient.getAddressById(
                context.getReq().getAddressId(),
                context.getUserId()
        );
        Asserts.notNull(address, BizErrorEnum.ORDER_CREATE_ADDRESS_NOT_EXIST);
        context.setAddress(address);
    }
}
