package com.example.fingerartbackend.service;

import com.example.fingerartbackend.auth.AuthContext;
import com.example.fingerartbackend.auth.AuthUser;
import com.example.fingerartbackend.auth.JwtTokenService;
import com.example.fingerartbackend.dto.TotpSetupResponse;
import com.example.fingerartbackend.dto.TotpStatusResponse;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.UserMapper;
import dev.samstevens.totp.exceptions.QrGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 管理端服务接口，定义业务能力（业务服务接口）。
 */
@Service
public class AdminTotpService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TotpService totpService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 查询管理端信息。
     */
    public TotpStatusResponse getStatus() {
        User admin = requireAdmin();
        TotpStatusResponse status = new TotpStatusResponse();
        status.setEnabled(Boolean.TRUE.equals(admin.getTotpEnabled()));
        return status;
    }

    /**
     * 执行 setup 相关逻辑。
     */
    public TotpSetupResponse setup() {
        User admin = requireAdmin();
        if (Boolean.TRUE.equals(admin.getTotpEnabled())) {
            throw new RuntimeException("已启用二次验证，请先关闭后再重新绑定");
        }
        String secret = totpService.generateSecret();
        TotpSetupResponse response = new TotpSetupResponse();
        response.setSecret(secret);
        response.setOtpAuthUrl(totpService.buildOtpAuthUrl(admin.getAccount(), secret));
        try {
            response.setQrCodeDataUri(totpService.generateQrDataUri(admin.getAccount(), secret));
        } catch (QrGenerationException e) {
            throw new RuntimeException("生成二维码失败");
        }
        return response;
    }

    /**
     * 执行 enable 相关逻辑。
     */
    public void enable(String secret, String code) {
        User admin = requireAdmin();
        if (Boolean.TRUE.equals(admin.getTotpEnabled())) {
            throw new RuntimeException("二次验证已启用");
        }
        if (secret == null || secret.isBlank()) {
            throw new RuntimeException("缺少密钥");
        }
        if (!totpService.verifyCode(secret, code)) {
            throw new RuntimeException("验证码错误，请确认 Authenticator 时间同步后重试");
        }
        admin.setTotpSecret(secret);
        admin.setTotpEnabled(true);
        userMapper.save(admin);
        adminAuditService.log("ENABLE_TOTP", "USER", admin.getId(), "启用 TOTP 二次验证");
    }

    /**
     * 执行 disable 相关逻辑。
     */
    public void disable(String code) {
        User admin = requireAdmin();
        if (!Boolean.TRUE.equals(admin.getTotpEnabled())) {
            throw new RuntimeException("尚未启用二次验证");
        }
        if (!totpService.verifyCode(admin.getTotpSecret(), code)) {
            throw new RuntimeException("验证码错误");
        }
        admin.setTotpSecret(null);
        admin.setTotpEnabled(false);
        userMapper.save(admin);
        adminAuditService.log("DISABLE_TOTP", "USER", admin.getId(), "关闭 TOTP 二次验证");
    }

    /**
     * 执行 verifyLoginTotp 相关逻辑。
     */
    public User verifyLoginTotp(String preAuthToken, String code) {
        if (preAuthToken == null || preAuthToken.isBlank()) {
            throw new RuntimeException("预认证已失效，请重新登录");
        }
        Long userId = jwtTokenService.parsePreAuthUserId(preAuthToken);
        if (userId == null) {
            throw new RuntimeException("预认证已失效，请重新登录");
        }
        User admin = userMapper.findById(userId)
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("非管理员账号");
        }
        if (!Boolean.TRUE.equals(admin.getTotpEnabled()) || admin.getTotpSecret() == null) {
            throw new RuntimeException("该账号未启用二次验证");
        }
        if (!totpService.verifyCode(admin.getTotpSecret(), code)) {
            throw new RuntimeException("验证码错误");
        }
        return admin;
    }

    /**
     * 执行 requireAdmin 相关逻辑。
     */
    private User requireAdmin() {
        AuthUser auth = AuthContext.get();
        if (auth == null || !"ADMIN".equals(auth.role())) {
            throw new RuntimeException("需要管理员权限");
        }
        User admin = userMapper.findById(auth.id())
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("需要管理员权限");
        }
        return admin;
    }
}
