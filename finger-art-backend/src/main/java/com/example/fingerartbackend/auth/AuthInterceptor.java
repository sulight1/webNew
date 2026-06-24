package com.example.fingerartbackend.auth;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.config.AuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.fingerartbackend.service.UserPunishmentService;

import java.util.regex.Pattern;

/**
 * 认证与鉴权拦截器。
 * <p>
 * 在 {@link JwtAuthFilter} 解析 Token 之后执行，负责：
 * <ol>
 *   <li>放行公开接口（登录、注册、作品浏览等）</li>
 *   <li>校验受保护接口是否已登录（401）</li>
 *   <li>校验管理员专属接口权限（403）</li>
 *   <li>校验非管理员账号是否被封禁（403）</li>
 * </ol>
 * 可通过 {@link AuthProperties#isEnforce()} 关闭强制校验（开发调试用）。
 * </p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** 公开用户资料接口路径模式：{@code /users/{id}/public} */
    private static final Pattern PUBLIC_USER_PROFILE = Pattern.compile("^/users/\\d+/public$");

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    @Lazy
    private UserPunishmentService userPunishmentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 请求前置鉴权：公开路径放行，受保护路径校验登录、角色与封禁状态。
     *
     * @return {@code true} 放行，{@code false} 拦截并返回 JSON 错误
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!authProperties.isEnforce()) {
            return true;
        }

        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String path = request.getRequestURI();

        if (isPublic(method, path)) {
            return true;
        }

        AuthUser user = AuthContext.get();
        if (user == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, Result.error(401, "请先登录"));
            return false;
        }

        if (requiresAdmin(method, path) && !isPrivilegedAdmin(user)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error(403, "需要管理员权限"));
            return false;
        }

        if (!isPrivilegedAdmin(user) && userPunishmentService.isAccountBanned(user.id())) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error(403, "账号已被封禁，如有疑问请联系平台"));
            return false;
        }

        return true;
    }

    /**
     * 判断接口是否为公开访问（无需登录）。
     */
    private boolean isPublic(String method, String path) {
        if (path.startsWith("/uploads/")) {
            return true;
        }
        if ("POST".equals(method) && ("/users/login".equals(path) || "/users/login/totp".equals(path)
                || "/users/register".equals(path)
                || "/users/password-reset-request".equals(path))) {
            return true;
        }
        if ("GET".equals(method)) {
            if ("/products".equals(path) || path.startsWith("/products/")) {
                return true;
            }
            if ("/custom-requests".equals(path) || path.matches("^/custom-requests/\\d+$")) {
                return true;
            }
            if ("/skills".equals(path)) {
                return true;
            }
            if (path.startsWith("/skills/") && !"/skills/mine".equals(path)) {
                return true;
            }
            if ("/search".equals(path)) {
                return true;
            }
            if (PUBLIC_USER_PROFILE.matcher(path).matches()) {
                return true;
            }
            if ("/ai/test".equals(path)) {
                return true;
            }
            if ("/craft-techniques".equals(path) || path.startsWith("/craft-techniques/")) {
                return true;
            }
            if ("/stats/summary".equals(path)) {
                return true;
            }
            if ("/users/top-artisans".equals(path)) {
                return true;
            }
            if ("/forum/posts".equals(path) || path.matches("^/forum/posts/\\d+$")
                    || path.matches("^/forum/posts/\\d+/replies$")) {
                return true;
            }
            if (path.matches("^/reviews/product/\\d+$")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否为已通过 TOTP 的特权管理员。
     */
    private boolean isPrivilegedAdmin(AuthUser user) {
        return user != null && "ADMIN".equals(user.role()) && user.adminSession();
    }

    /**
     * 判断接口是否需要管理员权限。
     */
    private boolean requiresAdmin(String method, String path) {
        if ("GET".equals(method) && "/users".equals(path)) {
            return true;
        }
        if (path.startsWith("/users/artisan-applications")) {
            return true;
        }
        if ("DELETE".equals(method) && path.matches("^/users/\\d+$")) {
            return true;
        }
        if (path.matches("^/users/\\d+/punishments.*")) {
            return true;
        }
        if ("GET".equals(method) && "/stats/platform".equals(path)) {
            return true;
        }
        if (path.startsWith("/reports")) {
            // 普通用户可提交举报；列表、统计、处理仍仅管理员
            if ("POST".equals(method) && "/reports".equals(path)) {
                return false;
            }
            return true;
        }
        if ("POST".equals(method) && "/users/add-coins".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && path.matches("^/users/\\d+/reset-password$")) {
            return true;
        }
        if (path.startsWith("/forum/admin")) {
            return true;
        }
        if (path.startsWith("/admin/")) {
            return true;
        }
        if ("POST".equals(method) && path.matches("^/products/\\d+/audit$")) {
            return true;
        }
        if ("POST".equals(method) && "/products/batch-audit".equals(path)) {
            return true;
        }
        if ("PATCH".equals(method) && path.matches("^/skills/\\d+/audit$")) {
            return true;
        }
        if ("POST".equals(method) && path.matches("^/custom-requests/\\d+/audit$")) {
            return true;
        }
        if ("POST".equals(method) && path.matches("^/orders/\\d+/resolve-dispute$")) {
            return true;
        }
        return false;
    }

    /**
     * 向客户端写入 JSON 格式的错误响应。
     */
    private void writeJson(HttpServletResponse response, int status, Result<?> body) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
