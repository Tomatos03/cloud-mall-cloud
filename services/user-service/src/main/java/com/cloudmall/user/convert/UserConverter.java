package com.cloudmall.user.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.user.api.request.RegisterReq;
import com.cloudmall.user.api.response.LoginResp;
import com.cloudmall.user.api.response.UserResp;
import com.cloudmall.user.entity.UserDO;

/**
 * UserDO 转换器
 * <p>
 * 提供 UserDO 到各种响应 DTO 的转换，以及 RegisterReq 到 UserDO 的转换。
 *
 * @author : Tomatos
 * @date : 2026/7/29
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * 转换登录响应，仅携带凭证 token。
     *
     * @param token 登录令牌
     * @return 处理结果
     */
    LoginResp toLoginResp(String token);

    /**
     * 转换对象数据。
     *
     * @param user 用户实体
     * @return 处理结果
     */
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "types", target = "userType")
    @Mapping(source = "avatarUrl", target = "avatar")
    UserResp toResp(UserDO user);

    /**
     * 转换对象列表。
     *
     * @param users 用户实体列表
     * @return 用户信息列表
     */
    List<UserResp> toRespList(List<UserDO> users);

    /**
     * 转换对象数据。
     *
     * @param req 请求参数
     * @return 处理结果
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    UserDO toDO(RegisterReq req);
}
