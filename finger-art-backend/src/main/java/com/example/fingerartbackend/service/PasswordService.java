package com.example.fingerartbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 密码服务接口，定义业务能力（业务服务接口）。
 */
@Service
public class PasswordService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 执行 encode 相关逻辑。
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 执行 matches 相关逻辑。
     */
    public boolean matches(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    /**
     * 执行 needsUpgrade 相关逻辑。
     */
    public boolean needsUpgrade(String storedPassword) {
        return storedPassword != null && !storedPassword.isBlank() && !isBcryptHash(storedPassword);
    }

    /**
     * 判断条件是否成立。
     */
    private boolean isBcryptHash(String storedPassword) {
        return storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$");
    }
}
