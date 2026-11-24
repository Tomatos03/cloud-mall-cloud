package com.cloudmall.coupon.api.client.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.exception.FallbackFailures;
import com.cloudmall.coupon.api.client.InternalCouponClient;
import com.cloudmall.coupon.api.model.req.AutoIssueRegisterReq;
import com.cloudmall.coupon.api.model.req.CouponOrderCommitByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderCommitReq;
import com.cloudmall.coupon.api.model.req.CouponOrderLockReq;
import com.cloudmall.coupon.api.model.req.CouponOrderQuoteReq;
import com.cloudmall.coupon.api.model.req.CouponOrderReleaseByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderReleaseReq;
import com.cloudmall.coupon.api.model.req.CouponOrderRevertByOrderReq;
import com.cloudmall.coupon.api.model.req.CouponOrderRevertReq;
import com.cloudmall.coupon.api.model.req.IssueCouponReq;
import com.cloudmall.coupon.api.model.resp.CouponOrderCalcResp;
import com.cloudmall.coupon.api.model.resp.UserCouponResp;

/**
 * 优惠券内部客户端兜底工厂
 * <p>
 * 锁券、核销、释放与回退均为订单协同写操作，失败必须抛出，交由 Saga 补偿。
 *
 * @author : Tomatos
 */
@Slf4j
public class InternalCouponClientFallbackFactory implements FallbackFactory<InternalCouponClient> {

    @Override
    public InternalCouponClient create(Throwable cause) {
        log.warn("优惠券内部客户端调用失败，快速失败", cause);
        RuntimeException failure = FallbackFailures.of(cause, BizErrorEnum.SERVICE_UNAVAILABLE);

        return new InternalCouponClient() {
            @Override
            public void autoIssueForRegister(AutoIssueRegisterReq req) {
                throw failure;
            }

            @Override
            public UserCouponResp issue(IssueCouponReq req) {
                throw failure;
            }

            @Override
            public CouponOrderCalcResp quote(CouponOrderQuoteReq req) {
                throw failure;
            }

            @Override
            public CouponOrderCalcResp lock(CouponOrderLockReq req) {
                throw failure;
            }

            @Override
            public void commit(CouponOrderCommitReq req) {
                throw failure;
            }

            @Override
            public void release(CouponOrderReleaseReq req) {
                throw failure;
            }

            @Override
            public void revert(CouponOrderRevertReq req) {
                throw failure;
            }

            @Override
            public void releaseByOrder(CouponOrderReleaseByOrderReq req) {
                throw failure;
            }

            @Override
            public void commitByOrder(CouponOrderCommitByOrderReq req) {
                throw failure;
            }

            @Override
            public void revertByOrder(CouponOrderRevertByOrderReq req) {
                throw failure;
            }

            @Override
            public List<UserCouponResp> listUserCoupons(long current, long size, String status) {
                throw failure;
            }
        };
    }
}
