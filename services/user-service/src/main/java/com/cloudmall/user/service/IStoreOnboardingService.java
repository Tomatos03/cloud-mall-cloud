package com.cloudmall.user.service;

import com.cloudmall.user.api.model.req.StoreRegisterReq;
import com.cloudmall.user.api.model.resp.StoreApplicationResp;

/**
 * 商家入驻服务。
 * <p>
 * 负责入驻资料提交、状态查询、平台审核与自动审核。
 *
 * @author : Tomatos
 */
public interface IStoreOnboardingService {

    /**
     * 查询当前用户的入驻状态。
     *
     * @param userId 用户 ID
     * @return 入驻申请状态（未提交时 status=NONE）
     */
    StoreApplicationResp getStatus(Long userId);

    /**
     * 提交或重新提交入驻申请。
     *
     * @param userId 用户 ID
     * @param req 入驻资料
     * @return 提交后的申请状态
     */
    StoreApplicationResp submit(Long userId, StoreRegisterReq req);

    /**
     * 审核入驻申请。
     *
     * @param applicationId 申请 ID
     * @param approved 是否通过
     * @param remark 审核意见/驳回原因
     * @param auditorId 审核人 ID
     * @return 审核后的申请状态
     */
    StoreApplicationResp review(Long applicationId, boolean approved, String remark, Long auditorId);

    /**
     * 自动审核超时的待审核申请。
     *
     * @param delaySeconds 提交后延迟秒数
     */
    void autoApprovePending(long delaySeconds);
}
