package com.example.fingerartbackend.auth;

public final class AuthContext {

    private static final ThreadLocal<AuthUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        CURRENT.set(user);
    }

    public static AuthUser get() {
        return CURRENT.get();
    }

    public static Long getUserId() {
        AuthUser user = CURRENT.get();
        return user != null ? user.id() : null;
    }

    public static boolean isAdmin() {
        AuthUser user = CURRENT.get();
        return user != null && "ADMIN".equals(user.role());
    }

    public static void clear() {
        CURRENT.remove();
    }
}
