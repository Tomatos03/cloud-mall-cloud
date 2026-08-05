package com.cloudmall.coupon.job;

import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.cloudmall.coupon.entity.UserCouponDO;
import com.cloudmall.coupon.mapper.UserCouponMapper;
import com.cloudmall.coupon.service.IUserCouponService;

/**
 * 用户券锁定过期释放 JobHandler
 * <p>
 * 兜底释放超过锁定过期时间仍处于 LOCKED 的用户券（订单取消/超时事件
 * 漏处理或服务宕机场景），避免用户券长期不可用。
 * 释放基于 per-coupon 流水幂等，重复执行无副作用。
 *
 * @author : Tomatos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponLockExpireJobHandler {

    /** 单批处理上限 */
    private static final int BATCH_SIZE = 200;

    /** 释放原因 */
    private static final String RELEASE_REASON = "锁定过期安全释放";

    private final UserCouponMapper userCouponMapper;
    private final IUserCouponService userCouponService;

    /**
     * 周期释放锁定过期的用户券。
     */
    @XxlJob("couponLockExpire")
    public void releaseExpiredLocks() {
        long pageNo = 1;
        List<UserCouponDO> expired;
        do {
            expired = listExpiredLocks(pageNo);
            if (CollUtil.isEmpty(expired)) {
                return;
            }
            for (UserCouponDO uc : expired) {
                releaseOne(uc);
            }
            pageNo++;
        } while (expired.size() >= BATCH_SIZE);
    }

    /**
     * 分页查询锁定过期用户券（按过期时间升序）。
     *
     * @param pageNo 页码
     * @return 用户券列表
     */
    private List<UserCouponDO> listExpiredLocks(long pageNo) {
        return userCouponMapper.selectExpiredLocks(new Page<>(pageNo, BATCH_SIZE), LocalDateTime.now())
                               .getRecords();
    }

    /**
     * 释放单张锁定过期用户券（幂等）。
     *
     * @param uc 用户券
     */
    private void releaseOne(UserCouponDO uc) {
        try {
            userCouponService.release(uc.getLockedOrderNo(), uc.getUserId(), uc.getId(), RELEASE_REASON);
        } catch (RuntimeException e) {
            log.warn("锁定过期释放失败 userCouponId={}", uc.getId(), e);
        }
    }
}
