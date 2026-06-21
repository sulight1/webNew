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

@Service
public class AdminAuditService {

    @Autowired
    private AdminOperationLogMapper logMapper;

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

    public Page<AdminOperationLog> listLogs(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return logMapper.findAllByOrderByCreatedAtDesc(pageable);
    }

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
