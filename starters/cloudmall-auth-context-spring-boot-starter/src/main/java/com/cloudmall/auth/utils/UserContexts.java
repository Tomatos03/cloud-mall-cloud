package com.cloudmall.auth.utils;

import com.cloudmall.common.context.UserContext;
import com.cloudmall.common.enums.UserTypeEnum;
import com.cloudmall.jwt.constant.JwtClaimNames;
import com.cloudmall.jwt.model.ParsedToken;

/**
 * 用户上下文转换工具
 * <p>
 * 作为 JWT starter 与 common 之间的桥接，将已校验的 {@link ParsedToken} 映射为
 * {@link UserContext}，统一网关与 WebSocket 握手等入口的用户语境构建逻辑。
 *
 * @author : Tomatos
 * @date : 2026/11/25
 */
public final class UserContexts {

    private UserContexts() {}

    /**
     * 将已解析的 token 转换为用户上下文。
     *
     * @param parsedToken 已校验的 token
     * @return 用户上下文
     * @throws IllegalArgumentException token 声明类型不匹配或
     *                                  userType 非法时抛出
     */
    public static UserContext of(ParsedToken parsedToken) {
        return UserContext.builder()
                          .userId(parsedToken.getLong(JwtClaimNames.USER_ID))
                          .username(parsedToken.getString(JwtClaimNames.USERNAME))
                          .userType(UserTypeEnum.from(parsedToken.getString(JwtClaimNames.USER_TYPE)))
                          .shopId(parsedToken.getLong(JwtClaimNames.SHOP_ID))
                          .permissionCodes(parsedToken.getStringList(JwtClaimNames.PERMISSION_CODES))
                          .build();
    }
}
