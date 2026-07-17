package com.cloudmall.auth.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.auth.api.request.RegisterReq;
import com.cloudmall.auth.api.response.LoginResp;
import com.cloudmall.auth.entity.AuthUserDO;

@Mapper(componentModel = "spring")
public interface AuthConverter {

    @Mapping(source = "id", target = "userId")
    LoginResp toLoginResp(AuthUserDO user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "storeId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    AuthUserDO toDO(RegisterReq req);
}
