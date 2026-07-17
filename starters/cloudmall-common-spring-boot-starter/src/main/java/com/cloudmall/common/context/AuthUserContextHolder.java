package com.cloudmall.common.context;

public final class AuthUserContextHolder {

    private static final ThreadLocal<AuthUserContext> CONTEXT = new ThreadLocal<>();

    private AuthUserContextHolder() {}

    public static void set(AuthUserContext context) {
        CONTEXT.set(context);
    }

    public static AuthUserContext get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        AuthUserContext c = get();
        return c != null ? c.getUserId() : null;
    }

    public static String getUsername() {
        AuthUserContext c = get();
        return c != null ? c.getUsername() : null;
    }

    public static Long getStoreId() {
        AuthUserContext c = get();
        return c != null ? c.getStoreId() : null;
    }

    public static String getUserType() {
        AuthUserContext c = get();
        return c != null ? c.getUserType() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
