package com.example.fingerartbackend.service;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.auth.AuthUser;
import com.example.fingerartbackend.entity.AdminOperationLog;
import com.example.fingerartbackend.mapper.AdminOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 管理端服务接口，定义业务能力（业务服务接口）。
 */
@Service
public class AdminAuditService {

    @Autowired
    private AdminOperationLogMapper logMapper;

    /**
     * 执行 log 相关逻辑。
     */
    public void log(String action, String targetType, Long targetId, String detail) {
        AuthUser admin = AuthContext.get();
        if (admin == null || !"ADMIN".equals(admin.role())) {
            return;
        }

        AdminOperationLog entry = new AdminOperationLog();
        entry.setAdminId(admin.id());
        entry.setAdminUsername(admin.username());
        entry.setAction(action);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setDetail(detail);
        entry.setIp(resolveClientIp());
        logMapper.save(entry);
    }

    /**
     * 查询管理端列表。
     */
    public Page<AdminOperationLog> listLogs(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return logMapper.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 执行 resolveClientIp 相关逻辑。
     */
    private String resolveClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
