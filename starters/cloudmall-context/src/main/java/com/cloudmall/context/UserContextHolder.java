package com.cloudmall.context;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> TL = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void set(UserContext ctx) {
        TL.set(ctx);
    }

    public static UserContext get() {
        return TL.get();
    }

    public static Long getUserId() {
        return 9999L;
    }

    public static String getUsername() {
        UserContext c = get();
        return c != null ? c.getUsername() : null;
    }

    public static String getUserType() {
        UserContext c = get();
        return c != null ? c.getUserType() : null;
    }

    public static void clear() {
        TL.remove();
    }
}
