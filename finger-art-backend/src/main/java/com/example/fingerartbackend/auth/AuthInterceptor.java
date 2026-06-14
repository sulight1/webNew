package com.example.fingerartbackend.auth;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.config.AuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Pattern;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Pattern PUBLIC_USER_PROFILE = Pattern.compile("^/users/\\d+/public$");

    @Autowired
    private AuthProperties authProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

        if (requiresAdmin(method, path) && !"ADMIN".equals(user.role())) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.error(403, "需要管理员权限"));
            return false;
        }

        return true;
    }

    private boolean isPublic(String method, String path) {
        if (path.startsWith("/uploads/")) {
            return true;
        }
        if ("POST".equals(method) && ("/users/login".equals(path) || "/users/register".equals(path)
                || "/users/password-reset-request".equals(path))) {
            return true;
        }
        if ("GET".equals(method)) {
            if ("/products".equals(path) || path.startsWith("/products/")) {
                return true;
            }
            if ("/custom-requests".equals(path)) {
                return true;
            }
            if ("/skills".equals(path) || path.startsWith("/skills/")) {
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
        }
        return false;
    }

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
        if ("GET".equals(method) && "/stats/platform".equals(path)) {
            return true;
        }
        if (path.startsWith("/reports")) {
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
        return false;
    }

    private void writeJson(HttpServletResponse response, int status, Result<?> body) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
