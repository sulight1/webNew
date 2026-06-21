package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.auth.LoginResponseFactory;
import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.LoginRequest;
import com.example.fingerartbackend.dto.LoginResponse;
import com.example.fingerartbackend.dto.RegisterRequest;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.AdminTotpService;
import com.example.fingerartbackend.service.UserService;
import com.example.fingerartbackend.service.UserPunishmentService;
import com.example.fingerartbackend.dto.ApplyUserPunishmentRequest;
import com.example.fingerartbackend.dto.LiftUserPunishmentsRequest;
import com.example.fingerartbackend.dto.UserPunishmentView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginResponseFactory loginResponseFactory;

    @Autowired
    private AdminAuditService adminAuditService;

    @Autowired
    private AdminTotpService adminTotpService;

    @Autowired
    private UserPunishmentService userPunishmentService;

    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request) {
        try {
            User registered = userService.register(request);
            return Result.success(loginResponseFactory.build(registered));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            User loggedIn = userService.login(request.getAccount(), request.getPassword());
            if ("ADMIN".equals(loggedIn.getRole()) && Boolean.TRUE.equals(loggedIn.getTotpEnabled())) {
                return Result.success(loginResponseFactory.buildRequiresTotp(loggedIn));
            }
            return Result.success(loginResponseFactory.build(loggedIn));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login/totp")
    public Result<LoginResponse> loginTotp(@RequestBody com.example.fingerartbackend.dto.TotpVerifyRequest request) {
        try {
            User admin = adminTotpService.verifyLoginTotp(request.getPreAuthToken(), request.getCode());
            return Result.success(loginResponseFactory.build(admin));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/password-reset-request")
    public Result<String> requestPasswordReset(@RequestBody java.util.Map<String, String> payload) {
        try {
            userService.requestPasswordReset(payload.get("account"));
            return Result.success("申请已提交，请等待管理员处理");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/apply-artisan")
    public Result<User> applyArtisan(@RequestParam Long userId) {
        try {
            return Result.success(userService.applyArtisan(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/artisan-applications/pending")
    public Result<List<User>> pendingArtisanApplications() {
        return Result.success(userService.listPendingArtisanApplications());
    }

    @PostMapping("/artisan-applications/{id}/approve")
    public Result<User> approveArtisan(@PathVariable Long id) {
        try {
            User user = userService.approveArtisan(id);
            adminAuditService.log("APPROVE_ARTISAN", "USER", id,
                    "通过手作达人申请：" + user.getUsername());
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/artisan-applications/{id}/reject")
    public Result<User> rejectArtisan(@PathVariable Long id) {
        try {
            User user = userService.rejectArtisan(id);
            adminAuditService.log("REJECT_ARTISAN", "USER", id,
                    "拒绝手作达人申请：" + user.getUsername());
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Result<User> getProfile(@RequestParam Long id) {
        try {
            return Result.success(userService.getUserById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/add-coins")
    public Result<User> addCoins(@RequestBody java.util.Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());
            User user = userService.addZaoWuBi(userId, amount);
            adminAuditService.log("ADD_COINS", "USER", userId,
                    "调整造物币 " + amount + "，当前余额 " + user.getZaowuBiBalance());
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/top-artisans")
    public Result<List<User>> topArtisans(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) Long excludeUserId) {
        return Result.success(userService.listTopArtisans(limit, excludeUserId));
    }

    @GetMapping
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(user -> user.setActivePunishments(userPunishmentService.getActiveViews(user.getId())));
        return Result.success(users);
    }

    @GetMapping("/{id}/punishments")
    public Result<List<UserPunishmentView>> getUserPunishments(@PathVariable Long id) {
        return Result.success(userPunishmentService.getActiveViews(id));
    }

    @PostMapping("/{id}/punishments")
    public Result<List<UserPunishmentView>> applyUserPunishments(
            @PathVariable Long id,
            @RequestBody ApplyUserPunishmentRequest request) {
        try {
            Long adminId = com.example.fingerartbackend.auth.AuthContext.getUserId();
            userPunishmentService.applyPunishments(id, adminId, request);
            adminAuditService.log("APPLY_PUNISHMENT", "USER", id,
                    "对用户 #" + id + " 执行处罚：" + String.join(",", request.getTypes()));
            return Result.success(userPunishmentService.getActiveViews(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/punishments/lift")
    public Result<List<UserPunishmentView>> liftUserPunishments(
            @PathVariable Long id,
            @RequestBody LiftUserPunishmentsRequest request) {
        try {
            Long adminId = com.example.fingerartbackend.auth.AuthContext.getUserId();
            userPunishmentService.liftPunishments(id, request.getTypes(), adminId);
            adminAuditService.log("LIFT_PUNISHMENT", "USER", id,
                    "解除用户 #" + id + " 的处罚：" + String.join(",", request.getTypes()));
            return Result.success(userPunishmentService.getActiveViews(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/punishments")
    public Result<List<UserPunishmentView>> liftAllUserPunishments(@PathVariable Long id) {
        try {
            Long adminId = com.example.fingerartbackend.auth.AuthContext.getUserId();
            userPunishmentService.liftAllPunishments(id, adminId);
            adminAuditService.log("LIFT_ALL_PUNISHMENTS", "USER", id, "清除用户 #" + id + " 的全部处罚");
            return Result.success(userPunishmentService.getActiveViews(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/punishments/{type}")
    public Result<List<UserPunishmentView>> liftUserPunishment(@PathVariable Long id, @PathVariable String type) {
        try {
            Long adminId = com.example.fingerartbackend.auth.AuthContext.getUserId();
            userPunishmentService.liftPunishment(id, type, adminId);
            adminAuditService.log("LIFT_PUNISHMENT", "USER", id, "解除用户 #" + id + " 的处罚：" + type);
            return Result.success(userPunishmentService.getActiveViews(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        try {
            User target = userService.getUserById(id);
            String password = payload.get("password");
            userService.resetPassword(id, password);
            adminAuditService.log("RESET_PASSWORD", "USER", id,
                    "重置用户密码：" + target.getUsername() + "（账号 " + target.getAccount() + "）");
            return Result.success("密码已重置");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            User target = userService.getUserById(id);
            userService.deleteUser(id);
            adminAuditService.log("DELETE_USER", "USER", id,
                    "删除用户：" + target.getUsername() + "（账号 " + target.getAccount() + "）");
            return Result.success("用户删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            return Result.success(userService.updateUser(id, user));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/follow")
    public Result<String> follow(@RequestBody java.util.Map<String, Long> payload) {
        try {
            userService.follow(payload.get("followerId"), payload.get("followingId"));
            return Result.success("关注成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/unfollow")
    public Result<String> unfollow(@RequestBody java.util.Map<String, Long> payload) {
        try {
            userService.unfollow(payload.get("followerId"), payload.get("followingId"));
            return Result.success("取消关注成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/is-following")
    public Result<Boolean> isFollowing(@RequestParam Long followerId, @RequestParam Long followingId) {
        return Result.success(userService.isFollowing(followerId, followingId));
    }

    @GetMapping("/{id}/followings")
    public Result<List<User>> getFollowings(@PathVariable Long id) {
        return Result.success(userService.getFollowings(id));
    }

    @GetMapping("/{id}/followers")
    public Result<List<User>> getFollowers(@PathVariable Long id) {
        return Result.success(userService.getFollowers(id));
    }

    @GetMapping("/{id}/public")
    public Result<com.example.fingerartbackend.dto.UserPublicProfile> getPublicProfile(@PathVariable Long id) {
        try {
            return Result.success(userService.getPublicProfile(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
