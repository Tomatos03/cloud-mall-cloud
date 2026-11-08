package com.cloudmall.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.api.enums.StoreApplicationStatusEnum;
import com.cloudmall.user.api.model.req.StoreRegisterReq;
import com.cloudmall.user.api.model.resp.StoreApplicationResp;
import com.cloudmall.user.constant.StoreConstants;
import com.cloudmall.user.convert.StoreApplicationConverter;
import com.cloudmall.user.mapper.StoreApplicationMapper;
import com.cloudmall.user.model.entity.StoreApplicationDO;
import com.cloudmall.user.service.IShopService;
import com.cloudmall.user.service.IStoreOnboardingService;
import com.cloudmall.user.service.IUserService;

/**
 * 商家入驻服务实现。
 *
 * @author : Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreOnboardingService implements IStoreOnboardingService {

    private final StoreApplicationMapper applicationMapper;
    private final StoreApplicationConverter applicationConverter;
    private final IShopService shopService;
    private final IUserService userService;

    /**
     * 查询当前用户的入驻状态。
     *
     * @param userId 用户 ID
     * @return 入驻申请状态
     */
    @Override
    public StoreApplicationResp getStatus(Long userId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        Optional<Long> shopId = shopService.findShopIdByUserId(userId);
        StoreApplicationDO application = applicationMapper.selectByUserId(userId);
        if (shopId.isPresent()) {
            StoreApplicationResp resp = application == null
                    ? StoreApplicationResp.builder().build()
                    : applicationConverter.toResp(application);
            resp.setStatus(StoreApplicationStatusEnum.SUCCESS);
            resp.setShopId(shopId.get());
            return resp;
        }
        if (application == null) {
            return StoreApplicationResp.builder()
                                       .status(StoreApplicationStatusEnum.NONE)
                                       .build();
        }
        return applicationConverter.toResp(application);
    }

    /**
     * 提交或重新提交入驻申请。
     *
     * @param userId 用户 ID
     * @param req 入驻资料
     * @return 提交后的申请状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreApplicationResp submit(Long userId, StoreRegisterReq req) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        Asserts.isFalse(
                shopService.findShopIdByUserId(userId).isPresent(),
                BizErrorEnum.STORE_APPLICATION_ALREADY_MERCHANT
        );
        validateLicense(req);
        StoreApplicationDO existing = applicationMapper.selectByUserId(userId);
        if (existing != null) {
            Asserts.isFalse(existing.getStatus().isPending(), BizErrorEnum.STORE_APPLICATION_PENDING);
            boolean opened = existing.getStatus().isSuccess();
            Asserts.isFalse(opened, BizErrorEnum.STORE_APPLICATION_ALREADY_MERCHANT);
        }
        StoreApplicationDO application = applicationConverter.toDO(req, userId);
        if (existing == null) {
            applicationMapper.insert(application);
        } else {
            application.setId(existing.getId());
            application.setCreateTime(existing.getCreateTime());
            applicationMapper.updateById(application);
            applicationMapper.clearAuditInfo(application.getId());
        }
        return applicationConverter.toResp(application);
    }

    /**
     * 审核入驻申请。
     *
     * @param applicationId 申请 ID
     * @param approved 是否通过
     * @param remark 审核意见/驳回原因
     * @param auditorId 审核人 ID
     * @return 审核后的申请状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreApplicationResp review(Long applicationId, boolean approved, String remark, Long auditorId) {
        StoreApplicationDO application = Asserts.requireNonNull(
                applicationMapper.selectById(applicationId),
                BizErrorEnum.STORE_APPLICATION_NOT_EXIST
        );
        Asserts.isTrue(application.getStatus().isPending(), BizErrorEnum.STORE_APPLICATION_STATUS_INVALID);
        StoreApplicationStatusEnum target = StoreApplicationStatusEnum.REJECTED;
        Long shopId = null;
        String finalRemark = StrUtil.trim(remark);
        if (approved) {
            shopId = shopService.createForUser(
                    application.getUserId(),
                    application.getStoreName(),
                    StoreConstants.DEFAULT_SHOP_DESCRIPTION
            );
            userService.grantMerchant(application.getUserId());
            target = StoreApplicationStatusEnum.SUCCESS;
            finalRemark = StrUtil.blankToDefault(finalRemark, "审核通过");
        } else {
            Asserts.notBlank(remark, BizErrorEnum.STORE_APPLICATION_REJECT_REASON_REQUIRED);
        }
        LocalDateTime auditTime = LocalDateTime.now().withNano(0);
        int updated = applicationMapper.markReviewed(
                applicationId,
                target,
                finalRemark,
                auditorId,
                auditTime,
                shopId
        );
        Asserts.isTrue(updated > 0, BizErrorEnum.STORE_APPLICATION_STATUS_INVALID);
        application.setStatus(target);
        application.setAuditRemark(finalRemark);
        application.setAuditorId(auditorId);
        application.setAuditTime(auditTime);
        application.setShopId(shopId);
        log.info(
                "商家入驻审核完成 applicationId={} userId={} approved={}",
                applicationId,
                application.getUserId(),
                approved
        );
        return applicationConverter.toResp(application);
    }

    /**
     * 自动审核超时的待审核申请。
     *
     * @param delaySeconds 提交后延迟秒数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoApprovePending(long delaySeconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(delaySeconds);
        List<StoreApplicationDO> applications = applicationMapper.selectPendingBefore(
                threshold,
                StoreConstants.AUTO_REVIEW_BATCH_SIZE
        );
        for (StoreApplicationDO application : applications) {
            review(application.getId(), true, StoreConstants.AUTO_APPROVE_REMARK, 0L);
        }
    }

    /**
     * 校验营业执照字段。
     *
     * @param req 入驻资料
     */
    private void validateLicense(StoreRegisterReq req) {
        if (!req.requiresLicense()) {
            return;
        }
        Asserts.notBlank(req.getLicenseNumber(), BizErrorEnum.STORE_APPLICATION_SUBMIT_INVALID);
        Asserts.notBlank(req.getLicenseName(), BizErrorEnum.STORE_APPLICATION_SUBMIT_INVALID);
        Asserts.notBlank(req.getEstablishmentDate(), BizErrorEnum.STORE_APPLICATION_SUBMIT_INVALID);
        Asserts.notBlank(req.getRegisteredAddress(), BizErrorEnum.STORE_APPLICATION_SUBMIT_INVALID);
        Asserts.notBlank(req.getLicensePhoto(), BizErrorEnum.STORE_APPLICATION_SUBMIT_INVALID);
    }
}
