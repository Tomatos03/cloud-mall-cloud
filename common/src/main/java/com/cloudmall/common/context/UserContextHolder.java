package com.cloudmall.common.context;

import com.cloudmall.common.enums.UserTypeEnum;

/**
 * 用户上下文持有者
 * <p>
 * 通过 ThreadLocal 存储当前请求的用户上下文信息，
 * 在请求链路中（Filter → Controller → Service）共享当前登录用户数据。
 * 请求结束后必须调用 {@link #clear()} 防止内存泄漏。
 *
 * @author : Tomatos
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> TL = new ThreadLocal<>();

    /**
     * 使用指定参数构造对象。
     */
    private UserContextHolder() {}

    /**
     * 设置当前请求的用户上下文。
     *
     * @param ctx 用户上下文
     */
    public static void set(UserContext ctx) {
        TL.set(ctx);
    }

    /**
     * 获取当前请求的用户上下文。
     *
     * @return 用户上下文，可能为 null
     */
    public static UserContext get() {
        return TL.get();
    }

    /**
     * 获取当前用户 ID。
     *
     * @return 用户 ID，未登录时返回 null
     */
    public static Long getUserId() {
        UserContext c = get();
        return c != null ? c.getUserId() : null;
    }

    /**
     * 获取当前用户名。
     *
     * @return 用户名，未登录时返回 null
     */
    public static String getUsername() {
        UserContext c = get();
        return c != null ? c.getUsername() : null;
    }

    /**
     * 获取当前用户类型。
     *
     * @return 用户类型，未登录时返回 null
     */
    public static UserTypeEnum getUserType() {
        UserContext c = get();
        return c != null ? c.getUserType() : null;
    }

    /**
     * 当前用户是否为管理员。
     *
     * @return true 表示管理员
     */
    public static boolean isAdmin() {
        UserContext c = get();
        return c != null && c.isAdmin();
    }

    /**
     * 当前用户是否为店铺商家。
     *
     * @return true 表示店铺商家
     */
    public static boolean isMerchant() {
        UserContext c = get();
        return c != null && c.isMerchant();
    }

    /**
     * 当前用户是否为普通用户。
     *
     * @return true 表示普通用户
     */
    public static boolean isNormal() {
        UserContext c = get();
        return c != null && c.isNormal();
    }

    /**
     * 获取当前用户店铺 ID。
     *
     * @return 店铺 ID，非商家或未登录时返回 null
     */
    public static Long getShopId() {
        UserContext c = get();
        return c != null ? c.getShopId() : null;
    }

    /**
     * 清除当前请求的用户上下文。 <p>
     * 务必在请求结束后调用，防止 ThreadLocal 内存泄漏。
     */
    public static void clear() {
        TL.remove();
    }
}
