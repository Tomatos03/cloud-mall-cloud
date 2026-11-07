package com.cloudmall.jwt.constant;

/**
 * JWT 声明名称。
 * <p>
 * 统一声明键名，避免调用方手写字符串导致读写不一致。
 *
 * @author : Tomatos
 * @date : 2026/11/7
 */
public interface JwtClaimNames {

    /** 用户 ID */
    String USER_ID = "userId";

    /** 用户名 */
    String USERNAME = "username";

    /** 用户类型 */
    String USER_TYPE = "userType";

    /** 店铺 ID */
    String SHOP_ID = "shopId";

    /** 权限码列表 */
    String PERMISSION_CODES = "permissionCodes";
}
