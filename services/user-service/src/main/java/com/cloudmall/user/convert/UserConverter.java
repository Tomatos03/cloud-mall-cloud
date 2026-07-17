package com.cloudmall.user.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.auth.api.response.UserInfoResp;
import com.cloudmall.user.entity.auth.AuthUserDO;

@Mapper(componentModel = "spring")
public interface UserConverter {

    @Mapping(source = "id", target = "userId")
    UserInfoResp toResp(AuthUserDO user);
}
