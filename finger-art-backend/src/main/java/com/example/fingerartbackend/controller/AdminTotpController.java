package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.TotpCodeRequest;
import com.example.fingerartbackend.dto.TotpEnableRequest;
import com.example.fingerartbackend.dto.TotpSetupResponse;
import com.example.fingerartbackend.dto.TotpStatusResponse;
import com.example.fingerartbackend.service.AdminTotpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 TOTP 二次验证控制器。
 * 负责管理员账号双因素认证的查询、绑定、启用与关闭，对应后台安全模块。
 */
@RestController
@RequestMapping("/admin/totp")
public class AdminTotpController {

    @Autowired
    private AdminTotpService adminTotpService;

    /**
     * 查询当前管理员的 TOTP 启用状态。
     *
     * @return TOTP 状态信息
     */
    @GetMapping("/status")
    public Result<TotpStatusResponse> status() {
        try {
            return Result.success(adminTotpService.getStatus());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发起 TOTP 绑定，生成密钥与二维码信息。
     *
     * @return 绑定所需的密钥及二维码数据
     */
    @PostMapping("/setup")
    public Result<TotpSetupResponse> setup() {
        try {
            return Result.success(adminTotpService.setup());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 验证并启用 TOTP 二次验证。
     *
     * @param request 含密钥与验证码的启用请求
     * @return 启用成功提示
     */
    @PostMapping("/enable")
    public Result<String> enable(@RequestBody TotpEnableRequest request) {
        try {
            adminTotpService.enable(request.getSecret(), request.getCode());
            return Result.success("二次验证已启用");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 验证验证码并关闭 TOTP 二次验证。
     *
     * @param request 含当前 TOTP 验证码的请求
     * @return 关闭成功提示
     */
    @PostMapping("/disable")
    public Result<String> disable(@RequestBody TotpCodeRequest request) {
        try {
            adminTotpService.disable(request.getCode());
            return Result.success("二次验证已关闭");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
