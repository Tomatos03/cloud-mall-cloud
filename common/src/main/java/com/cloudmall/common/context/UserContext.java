package com.cloudmall.common.context;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cloudmall.common.enums.UserTypeEnum;

/**
 * 当前请求用户上下文
 * <p>
 * 通过 {@link UserContextHolder} 在当前请求线程内共享，跨服务透传时以
 * JSON 形式写入 X-Auth-User 请求头，userType 以枚举 code 序列化。
 *
 * @author : Tomatos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private Long userId;
    private String username;
    private UserTypeEnum userType;
    private Long shopId;
    private List<String> permissionCodes;
    private Map<String, Object> extra;

    /**
     * 当前用户是否为管理员。
     *
     * @return true 表示管理员
     */
    public boolean isAdmin() {
        return UserTypeEnum.ADMIN.equals(this.userType);
    }

    /**
     * 当前用户是否为店铺商家。
     *
     * @return true 表示店铺商家
     */
    public boolean isMerchant() {
        return UserTypeEnum.MERCHANT.equals(this.userType);
    }

    /**
     * 当前用户是否为普通用户。
     *
     * @return true 表示普通用户
     */
    public boolean isNormal() {
        return UserTypeEnum.NORMAL.equals(this.userType);
    }
}
