package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.TotpCodeRequest;
import com.example.fingerartbackend.dto.TotpEnableRequest;
import com.example.fingerartbackend.dto.TotpSetupResponse;
import com.example.fingerartbackend.dto.TotpStatusResponse;
import com.example.fingerartbackend.service.AdminTotpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/admin/totp")
public class AdminTotpController {

    @Autowired
    private AdminTotpService adminTotpService;

    @GetMapping("/status")
    public Result<TotpStatusResponse> status() {
        try {
            return Result.success(adminTotpService.getStatus());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/setup")
    public Result<TotpSetupResponse> setup() {
        try {
            return Result.success(adminTotpService.setup());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/enable")
    public Result<String> enable(@RequestBody TotpEnableRequest request) {
        try {
            adminTotpService.enable(request.getSecret(), request.getCode());
            return Result.success("二次验证已启用");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

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
