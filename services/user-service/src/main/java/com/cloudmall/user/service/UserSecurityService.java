package com.cloudmall.user.service;

import java.time.Duration;
import java.time.LocalDateTime;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.api.model.req.RealNameReviewReq;
import com.cloudmall.user.api.model.req.SetPayPasswordReq;
import com.cloudmall.user.api.model.req.SubmitRealNameReq;
import com.cloudmall.user.api.model.req.VerifyPayPasswordReq;
import com.cloudmall.user.api.model.resp.RealNameResp;
import com.cloudmall.user.api.model.resp.UserSecurityResp;
import com.cloudmall.user.convert.UserSecurityConverter;
import com.cloudmall.user.mapper.UserMapper;
import com.cloudmall.user.mapper.UserSecurityMapper;
import com.cloudmall.user.model.entity.UserSecurityDO;
import com.cloudmall.user.security.IdCardCipher;
import com.cloudmall.user.security.IdCardValidator;
import com.cloudmall.user.security.MaskUtil;

/**
 * 当前用户账户安全服务：支付密码、实名认证。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private static final String PAY_PASSWORD_FAIL_KEY_PREFIX = "cloudmall:user:pay-password:fail:";
    private static final int PAY_PASSWORD_MAX_FAILURES = 5;
    private static final Duration PAY_PASSWORD_FAIL_TTL = Duration.ofMinutes(10);

    private final UserMapper userMapper;
    private final UserSecurityMapper userSecurityMapper;
    private final UserSecurityConverter userSecurityConverter;
    private final PasswordEncoder passwordEncoder;
    private final IdCardCipher idCardCipher;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 查询当前用户安全信息。
     *
     * @param userId 用户 ID
     * @return 安全信息
     */
    public UserSecurityResp getSecurity(Long userId) {
        UserSecurityDO security = getOrCreate(userId);
        return userSecurityConverter.toSecurityResp(security);
    }

    /**
     * 设置或修改支付密码。
     *
     * @param userId 用户 ID
     * @param req 请求参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void setPayPassword(Long userId, SetPayPasswordReq req) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        UserSecurityDO security = getOrCreate(userId);
        String currentHash = security.getPayPasswordHash();
        if (StrUtil.isNotBlank(currentHash)) {
            boolean oldPasswordPresent = StrUtil.isNotBlank(req.getOldPassword());
            Asserts.isTrue(oldPasswordPresent, BizErrorEnum.PAY_PASSWORD_ERROR);
            Asserts.isTrue(
                    passwordEncoder.matches(req.getOldPassword(), currentHash),
                    BizErrorEnum.PAY_PASSWORD_ERROR
            );
            Asserts.isFalse(
                    passwordEncoder.matches(req.getNewPassword(), currentHash),
                    BizErrorEnum.INVALID_PARAM
            );
        }
        String encoded = passwordEncoder.encode(req.getNewPassword());
        LocalDateTime now = LocalDateTime.now();
        userSecurityMapper.update(
                null,
                Wrappers.<UserSecurityDO>lambdaUpdate()
                        .set(UserSecurityDO::getPayPasswordHash, encoded)
                        .set(UserSecurityDO::getPayPasswordSetTime, now)
                        .eq(UserSecurityDO::getUserId, userId)
        );
        security.setPayPasswordHash(encoded);
        security.setPayPasswordSetTime(now);
        clearPayPasswordFailures(userId);
    }

    /**
     * 校验支付密码。
     *
     * @param userId 用户 ID
     * @param req 请求参数
     * @return true 表示校验通过
     */
    public boolean verifyPayPassword(Long userId, VerifyPayPasswordReq req) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        UserSecurityDO security = getOrCreate(userId);
        Asserts.notNull(security.getPayPasswordHash(), BizErrorEnum.PAY_PASSWORD_NOT_SET);
        checkPayPasswordRate(userId);
        if (!passwordEncoder.matches(req.getPassword(), security.getPayPasswordHash())) {
            recordPayPasswordFailure(userId);
            throw new com.cloudmall.common.exception.BizException(BizErrorEnum.PAY_PASSWORD_ERROR);
        }
        clearPayPasswordFailures(userId);
        return true;
    }

    /**
     * 提交实名认证。
     *
     * @param userId 用户 ID
     * @param req 请求参数
     * @return 实名认证信息
     */
    @Transactional(rollbackFor = Exception.class)
    public RealNameResp submitRealName(Long userId, SubmitRealNameReq req) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        Asserts.requireNonNull(userMapper.selectById(userId), BizErrorEnum.USER_NOT_EXISTS);
        Asserts.isTrue(IdCardValidator.isValid(req.getIdCardNo()), BizErrorEnum.REAL_NAME_INVALID);

        UserSecurityDO security = getOrCreate(userId);
        Asserts.isFalse(
                "VERIFIED".equals(security.getRealNameStatus()),
                BizErrorEnum.REAL_NAME_ALREADY_VERIFIED
        );
        Asserts.isFalse("PENDING".equals(security.getRealNameStatus()), BizErrorEnum.REAL_NAME_PENDING);

        LocalDateTime now = LocalDateTime.now();
        security.setRealName(idCardCipher.encrypt(req.getRealName()));
        security.setRealNameMask(MaskUtil.maskName(req.getRealName()));
        security.setIdCardNo(idCardCipher.encrypt(req.getIdCardNo()));
        security.setIdCardMask(MaskUtil.maskIdCard(req.getIdCardNo()));
        security.setRealNameStatus("PENDING");
        security.setRealNameSubmitTime(now);
        security.setRealNameVerifyTime(null);
        security.setRealNameRejectReason(null);
        userSecurityMapper.updateById(security);
        return userSecurityConverter.toRealNameResp(security);
    }

    /**
     * 查询当前用户实名认证信息。
     *
     * @param userId 用户 ID
     * @return 实名认证信息
     */
    public RealNameResp getRealName(Long userId) {
        return userSecurityConverter.toRealNameResp(getOrCreate(userId));
    }

    /**
     * 内部审核实名认证。
     *
     * @param req 审核请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void reviewRealName(RealNameReviewReq req) {
        Asserts.notNull(req.getUserId(), BizErrorEnum.INVALID_PARAM);
        Asserts.isTrue(
                "VERIFIED".equals(req.getStatus()) || "REJECTED".equals(req.getStatus()),
                BizErrorEnum.INVALID_AUDIT_STATUS
        );
        UserSecurityDO security = Asserts.requireNonNull(
                userSecurityMapper.selectById(req.getUserId()),
                BizErrorEnum.REAL_NAME_NOT_FOUND
        );
        Asserts.isTrue("PENDING".equals(security.getRealNameStatus()), BizErrorEnum.INVALID_AUDIT_STATUS);
        security.setRealNameStatus(req.getStatus());
        security.setRealNameVerifyTime(LocalDateTime.now());
        security.setRealNameRejectReason("REJECTED".equals(req.getStatus()) ? req.getRejectReason() : null);
        userSecurityMapper.updateById(security);
    }

    /**
     * 获取或初始化安全信息。
     *
     * @param userId 用户 ID
     * @return 安全信息
     */
    private UserSecurityDO getOrCreate(Long userId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        UserSecurityDO security = userSecurityMapper.selectById(userId);
        if (security != null) {
            return security;
        }
        security = UserSecurityDO.builder()
                                 .userId(userId)
                                 .realNameStatus("UNVERIFIED")
                                 .build();
        userSecurityMapper.insert(security);
        return security;
    }

    /**
     * 检查支付密码失败次数。
     *
     * @param userId 用户 ID
     */
    private void checkPayPasswordRate(Long userId) {
        String value = stringRedisTemplate.opsForValue().get(failKey(userId));
        if (value != null && Integer.parseInt(value) >= PAY_PASSWORD_MAX_FAILURES) {
            throw new com.cloudmall.common.exception.BizException(BizErrorEnum.PAY_PASSWORD_RETRY_LIMIT);
        }
    }

    /**
     * 记录一次支付密码失败。
     *
     * @param userId 用户 ID
     */
    private void recordPayPasswordFailure(Long userId) {
        String key = failKey(userId);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, PAY_PASSWORD_FAIL_TTL);
        }
    }

    /**
     * 清除支付密码失败计数。
     *
     * @param userId 用户 ID
     */
    private void clearPayPasswordFailures(Long userId) {
        stringRedisTemplate.delete(failKey(userId));
    }

    /**
     * 支付密码失败计数 Key。
     *
     * @param userId 用户 ID
     * @return Redis Key
     */
    private String failKey(Long userId) {
        return PAY_PASSWORD_FAIL_KEY_PREFIX + userId;
    }
}
