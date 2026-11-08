package com.cloudmall.user.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cloudmall.user.api.model.resp.RealNameResp;
import com.cloudmall.user.api.model.resp.UserSecurityResp;
import com.cloudmall.user.model.entity.UserSecurityDO;

/**
 * 账户安全信息转换器。
 *
 * @author : Tomatos
 */
@Mapper(componentModel = "spring")
public interface UserSecurityConverter {

    /**
     * 安全信息实体 -> 安全信息响应。
     *
     * @param security 安全信息实体
     * @return 安全信息响应
     */
    @Mapping(source = "idCardMask", target = "idCardMasked")
    @Mapping(source = "realNameMask", target = "realNameMasked")
    @Mapping(source = "realNameSubmitTime", target = "submitTime")
    @Mapping(source = "realNameVerifyTime", target = "verifyTime")
    @Mapping(source = "realNameRejectReason", target = "rejectReason")
    @Mapping(target = "payPasswordSet", expression = "java(security.getPayPasswordHash() != null)")
    UserSecurityResp toSecurityResp(UserSecurityDO security);

    /**
     * 安全信息实体 -> 实名认证响应。
     *
     * @param security 安全信息实体
     * @return 实名认证响应
     */
    @Mapping(source = "realNameStatus", target = "status")
    @Mapping(source = "idCardMask", target = "idCardMasked")
    @Mapping(source = "realNameMask", target = "realNameMasked")
    @Mapping(source = "realNameSubmitTime", target = "submitTime")
    @Mapping(source = "realNameVerifyTime", target = "verifyTime")
    @Mapping(source = "realNameRejectReason", target = "rejectReason")
    RealNameResp toRealNameResp(UserSecurityDO security);
}
