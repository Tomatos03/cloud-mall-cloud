package com.cloudmall.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserContext {
    private Long userId;
    private String username;
    private Long storeId;
    private String userType; // "web" | "manager" | "merchant"

    public static AuthUserContext fromAuthUser(Object authUser) {
        // 反射从 AuthUser 对象中取字段，各服务自己实现具体转换
        return AuthUserContext.builder().build();
    }
}
