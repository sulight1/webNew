package com.example.fingerartbackend.auth;

/**
 * 当前请求认证用户上下文（ThreadLocal）。
 * <p>
 * 由 {@link JwtAuthFilter} 在解析 JWT 后写入，供 Controller/Service 层获取当前登录用户信息；
 * 请求结束时在 Filter 的 finally 块中清理，避免线程复用导致的数据泄漏。
 * </p>
 */
public final class AuthContext {

    private static final ThreadLocal<AuthUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    /**
     * 设置当前请求的认证用户。
     *
     * @param user 已解析的认证用户
     */
    public static void set(AuthUser user) {
        CURRENT.set(user);
    }

    /**
     * 获取当前请求的认证用户，未登录时返回 {@code null}。
     *
     * @return 当前认证用户
     */
    public static AuthUser get() {
        return CURRENT.get();
    }

    /**
     * 获取当前登录用户 ID，未登录时返回 {@code null}。
     *
     * @return 用户 ID
     */
    public static Long getUserId() {
        AuthUser user = CURRENT.get();
        return user != null ? user.id() : null;
    }

    /**
     * 判断当前用户是否为已通过 TOTP 验证的管理员会话。
     *
     * @return 是否为特权管理员
     */
    public static boolean isAdmin() {
        AuthUser user = CURRENT.get();
        return user != null && "ADMIN".equals(user.role()) && user.adminSession();
    }

    /**
     * 清除当前线程的认证上下文。
     */
    public static void clear() {
        CURRENT.remove();
    }
}
