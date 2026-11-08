package com.cloudmall.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudmall.common.enums.BizErrorEnum;
import com.cloudmall.common.utils.Asserts;
import com.cloudmall.user.api.model.req.ChangePasswordReq;
import com.cloudmall.user.api.model.req.UpdateUserProfileReq;
import com.cloudmall.user.api.model.req.UserSettingsUpdateReq;
import com.cloudmall.user.api.model.resp.UserProfileResp;
import com.cloudmall.user.api.model.resp.UserSettingsResp;
import com.cloudmall.user.convert.UserProfileConverter;
import com.cloudmall.user.mapper.UserMapper;
import com.cloudmall.user.mapper.UserProfileMapper;
import com.cloudmall.user.model.entity.UserDO;
import com.cloudmall.user.model.entity.UserProfileDO;

/**
 * 当前登录用户的资料与设置服务。
 *
 * @author : Tomatos
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileConverter userProfileConverter;

    /**
     * 查询当前用户完整资料。
     *
     * @param userId 用户 ID
     * @return 用户资料
     */
    public UserProfileResp getProfile(Long userId) {
        UserDO user = requireUser(userId);
        UserProfileDO profile = getOrCreateProfile(userId);
        return userProfileConverter.toProfileResp(user, profile);
    }

    /**
     * 更新当前用户资料。
     *
     * @param userId 用户 ID
     * @param req 更新参数
     * @return 更新后的用户资料
     */
    @Transactional(rollbackFor = Exception.class)
    public UserProfileResp updateProfile(Long userId, UpdateUserProfileReq req) {
        UserDO user = requireUser(userId);
        UserProfileDO profile = getOrCreateProfile(userId);

        var update = Wrappers.<UserDO>lambdaUpdate().eq(UserDO::getId, userId);
        boolean userChanged = false;
        if (req.getNickname() != null) {
            update.set(UserDO::getNickname, req.getNickname());
            userChanged = true;
            user.setNickname(req.getNickname());
        }
        if (req.getAvatar() != null) {
            update.set(UserDO::getAvatarUrl, req.getAvatar());
            userChanged = true;
            user.setAvatarUrl(req.getAvatar());
        }
        if (req.getPhone() != null) {
            update.set(UserDO::getPhone, req.getPhone());
            userChanged = true;
            user.setPhone(req.getPhone());
        }
        if (req.getEmail() != null) {
            update.set(UserDO::getEmail, req.getEmail());
            userChanged = true;
            user.setEmail(req.getEmail());
        }
        if (req.getBio() != null) {
            update.set(UserDO::getBio, req.getBio());
            userChanged = true;
            user.setBio(req.getBio());
        }
        if (userChanged) {
            userMapper.update(null, update);
        }

        boolean profileChanged = false;
        if (req.getGender() != null) {
            profile.setGender(req.getGender());
            profileChanged = true;
        }
        if (req.getBirthday() != null) {
            profile.setBirthday(req.getBirthday());
            profileChanged = true;
        }
        if (profileChanged) {
            saveProfile(profile);
        }

        return userProfileConverter.toProfileResp(user, profile);
    }

    /**
     * 修改当前用户登录密码。
     *
     * @param userId 用户 ID
     * @param req 密码参数
     */
    public void changePassword(Long userId, ChangePasswordReq req) {
        UserDO user = requireUser(userId);
        Asserts.isTrue(
                passwordEncoder.matches(req.getOldPassword(), user.getPassword()),
                BizErrorEnum.PASSWORD_ERROR
        );
        UserDO update = UserDO.builder()
                              .id(userId)
                              .password(passwordEncoder.encode(req.getNewPassword()))
                              .build();
        userMapper.updateById(update);
    }

    /**
     * 查询当前用户通知与隐私设置。
     *
     * @param userId 用户 ID
     * @return 设置
     */
    public UserSettingsResp getSettings(Long userId) {
        UserProfileDO profile = getOrCreateProfile(userId);
        return userProfileConverter.toSettingsResp(profile);
    }

    /**
     * 更新当前用户通知与隐私设置。
     *
     * @param userId 用户 ID
     * @param req 更新参数
     * @return 更新后的设置
     */
    @Transactional(rollbackFor = Exception.class)
    public UserSettingsResp updateSettings(Long userId, UserSettingsUpdateReq req) {
        UserProfileDO profile = getOrCreateProfile(userId);
        if (req.getProfileVisibility() != null) {
            profile.setProfileVisibility(req.getProfileVisibility());
        }
        if (req.getPurchaseVisibility() != null) {
            profile.setPurchaseVisibility(req.getPurchaseVisibility());
        }
        if (req.getNotifyOrder() != null) {
            profile.setNotifyOrder(req.getNotifyOrder());
        }
        if (req.getNotifyPromotion() != null) {
            profile.setNotifyPromotion(req.getNotifyPromotion());
        }
        if (req.getNotifyLogistics() != null) {
            profile.setNotifyLogistics(req.getNotifyLogistics());
        }
        if (req.getNotifyCommunity() != null) {
            profile.setNotifyCommunity(req.getNotifyCommunity());
        }
        if (req.getRecommendEnabled() != null) {
            profile.setRecommendEnabled(req.getRecommendEnabled());
        }
        saveProfile(profile);
        return userProfileConverter.toSettingsResp(profile);
    }

    /**
     * 查询用户，不存在时抛出业务异常。
     *
     * @param userId 用户 ID
     * @return 用户实体
     */
    private UserDO requireUser(Long userId) {
        Asserts.notNull(userId, BizErrorEnum.USER_NOT_AUTHENTICATED);
        return Asserts.requireNonNull(userMapper.selectById(userId), BizErrorEnum.USER_NOT_EXISTS);
    }

    /**
     * 获取扩展资料，不存在时初始化默认值。
     *
     * @param userId 用户 ID
     * @return 扩展资料
     */
    private UserProfileDO getOrCreateProfile(Long userId) {
        UserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile != null) {
            return profile;
        }
        profile = defaultProfile(userId);
        userProfileMapper.insert(profile);
        return profile;
    }

    /**
     * 构建默认扩展资料。
     *
     * @param userId 用户 ID
     * @return 默认扩展资料
     */
    private UserProfileDO defaultProfile(Long userId) {
        return UserProfileDO.builder()
                            .userId(userId)
                            .gender("保密")
                            .profileVisibility("PUBLIC")
                            .purchaseVisibility("PRIVATE")
                            .notifyOrder(true)
                            .notifyPromotion(true)
                            .notifyLogistics(true)
                            .notifyCommunity(false)
                            .recommendEnabled(true)
                            .build();
    }

    /**
     * 保存扩展资料，不存在时插入。
     *
     * @param profile 扩展资料
     */
    private void saveProfile(UserProfileDO profile) {
        if (userProfileMapper.selectByUserId(profile.getUserId()) == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
    }

}
