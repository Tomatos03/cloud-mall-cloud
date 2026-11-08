package com.cloudmall.user.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.user.api.model.resp.BrowseHistoryResp;
import com.cloudmall.user.api.model.resp.UserPointLogResp;
import com.cloudmall.user.api.model.resp.UserPointSummaryResp;
import com.cloudmall.user.api.model.resp.UserProfileResp;
import com.cloudmall.user.api.model.resp.UserSettingsResp;
import com.cloudmall.user.model.entity.BrowseHistoryDO;
import com.cloudmall.user.model.entity.UserDO;
import com.cloudmall.user.model.entity.UserPointDO;
import com.cloudmall.user.model.entity.UserPointLogDO;
import com.cloudmall.user.model.entity.UserProfileDO;

/**
 * 个人中心扩展资料转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface UserProfileConverter {

    /**
     * 用户 + 扩展资料 -> 完整个人资料。
     *
     * @param user 用户实体
     * @param profile 扩展资料实体
     * @return 个人资料响应
     */
    @Mapping(source = "user.bio", target = "bio")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.phone", target = "phone")
    @Mapping(source = "user.types", target = "userType")
    @Mapping(source = "profile.gender", target = "gender")
    @Mapping(source = "user.avatarUrl", target = "avatar")
    @Mapping(source = "user.nickname", target = "nickname")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "profile.birthday", target = "birthday")
    UserProfileResp toProfileResp(UserDO user, UserProfileDO profile);

    /**
     * 扩展资料 -> 通知与隐私设置。
     *
     * @param profile 扩展资料实体
     * @return 设置响应
     */
    UserSettingsResp toSettingsResp(UserProfileDO profile);

    /**
     * 积分账户 -> 积分汇总。
     *
     * @param point 积分账户实体
     * @return 积分汇总响应
     */
    UserPointSummaryResp toPointSummaryResp(UserPointDO point);

    /**
     * 积分流水 -> 流水响应。
     *
     * @param log 积分流水实体
     * @return 积分流水响应
     */
    UserPointLogResp toPointLogResp(UserPointLogDO log);

    /**
     * 积分流水列表 -> 流水响应列表。
     *
     * @param logs 积分流水实体列表
     * @return 积分流水响应列表
     */
    List<UserPointLogResp> toPointLogRespList(List<UserPointLogDO> logs);

    /**
     * 浏览记录 -> 浏览记录响应。
     *
     * @param history 浏览记录实体
     * @return 浏览记录响应
     */
    BrowseHistoryResp toBrowseHistoryResp(BrowseHistoryDO history);

    /**
     * 浏览记录列表 -> 浏览记录响应列表。
     *
     * @param histories 浏览记录实体列表
     * @return 浏览记录响应列表
     */
    List<BrowseHistoryResp> toBrowseHistoryRespList(List<BrowseHistoryDO> histories);
}
