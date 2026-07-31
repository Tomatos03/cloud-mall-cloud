package com.cloudmall.auth.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.auth.entity.UserDO;

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

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "types", target = "userType")
    LoginResp toLoginResp(UserDO user);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "avatarUrl", target = "avatar")
    @Mapping(source = "types", target = "userType")
    UserInfoResp toResp(UserDO user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    UserDO toDO(RegisterReq req);
}
