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

/**
 * 用户控制器。
 * 负责注册登录、资料管理、达人申请、关注关系及用户处罚，对应用户与权限管理模块。
 */
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

    /**
     * 用户注册并自动登录。
     *
     * @param request 注册请求（账号、密码、用户名等）
     * @return 登录响应（含 Token）
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request) {
        try {
            User registered = userService.register(request);
            return Result.success(loginResponseFactory.build(registered));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录，管理员启用 TOTP 时需二次验证。
     *
     * @param request 登录请求（账号、密码）
     * @return 登录响应或 TOTP 预认证 Token
     */
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

    /**
     * 管理员 TOTP 二次验证登录。
     *
     * @param request 含 preAuthToken 与验证码的请求
     * @return 完整登录响应
     */
    @PostMapping("/login/totp")
    public Result<LoginResponse> loginTotp(@RequestBody com.example.fingerartbackend.dto.TotpVerifyRequest request) {
        try {
            User admin = adminTotpService.verifyLoginTotp(request.getPreAuthToken(), request.getCode());
            return Result.success(loginResponseFactory.build(admin));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交密码重置申请。
     *
     * @param payload 含 account 字段的请求体
     * @return 申请提交成功提示
     */
    @PostMapping("/password-reset-request")
    public Result<String> requestPasswordReset(@RequestBody java.util.Map<String, String> payload) {
        try {
            userService.requestPasswordReset(payload.get("account"));
            return Result.success("申请已提交，请等待管理员处理");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户申请成为手作达人。
     *
     * @param userId 用户 ID
     * @return 更新后的用户信息
     */
    @PostMapping("/apply-artisan")
    public Result<User> applyArtisan(@RequestParam Long userId) {
        try {
            return Result.success(userService.applyArtisan(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询待审核的手作达人申请列表。
     *
     * @return 待审核用户列表
     */
    @GetMapping("/artisan-applications/pending")
    public Result<List<User>> pendingArtisanApplications() {
        return Result.success(userService.listPendingArtisanApplications());
    }

    /**
     * 管理员通过手作达人申请。
     *
     * @param id 用户 ID
     * @return 更新后的用户信息
     */
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

    /**
     * 管理员拒绝手作达人申请。
     *
     * @param id 用户 ID
     * @return 更新后的用户信息
     */
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

    /**
     * 查询用户个人资料。
     *
     * @param id 用户 ID
     * @return 用户实体
     */
    @GetMapping("/profile")
    public Result<User> getProfile(@RequestParam Long id) {
        try {
            return Result.success(userService.getUserById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员调整用户造物币余额。
     *
     * @param payload 含 userId、amount 的请求体
     * @return 更新后的用户信息
     */
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

    /**
     * 获取评分最高的手作达人列表。
     *
     * @param limit         返回数量上限，默认 5
     * @param excludeUserId 可选排除的用户 ID
     * @return 达人用户列表
     */
    @GetMapping("/top-artisans")
    public Result<List<User>> topArtisans(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) Long excludeUserId) {
        return Result.success(userService.listTopArtisans(limit, excludeUserId));
    }

    /**
     * 查询全部用户列表（含当前生效的处罚信息）。
     *
     * @return 用户列表
     */
    @GetMapping
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(user -> user.setActivePunishments(userPunishmentService.getActiveViews(user.getId())));
        return Result.success(users);
    }

    /**
     * 查询用户当前生效的处罚列表。
     *
     * @param id 用户 ID
     * @return 处罚视图列表
     */
    @GetMapping("/{id}/punishments")
    public Result<List<UserPunishmentView>> getUserPunishments(@PathVariable Long id) {
        return Result.success(userPunishmentService.getActiveViews(id));
    }

    /**
     * 管理员对用户执行处罚。
     *
     * @param id      用户 ID
     * @param request 处罚类型及详情
     * @return 更新后的处罚列表
     */
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

    /**
     * 管理员解除用户指定类型的处罚。
     *
     * @param id      用户 ID
     * @param request 要解除的处罚类型列表
     * @return 更新后的处罚列表
     */
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

    /**
     * 管理员清除用户全部处罚。
     *
     * @param id 用户 ID
     * @return 更新后的处罚列表（应为空）
     */
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

    /**
     * 管理员解除用户某一类型的处罚。
     *
     * @param id   用户 ID
     * @param type 处罚类型
     * @return 更新后的处罚列表
     */
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

    /**
     * 管理员重置用户密码。
     *
     * @param id      用户 ID
     * @param payload 含 password 字段的请求体
     * @return 重置成功提示
     */
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

    /**
     * 管理员删除用户。
     *
     * @param id 用户 ID
     * @return 删除成功提示
     */
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

    /**
     * 更新用户资料。
     *
     * @param id   用户 ID
     * @param user 更新后的用户数据
     * @return 更新后的用户实体
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            return Result.success(userService.updateUser(id, user));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 关注指定用户。
     *
     * @param payload 含 followerId、followingId 的请求体
     * @return 关注成功提示
     */
    @PostMapping("/follow")
    public Result<String> follow(@RequestBody java.util.Map<String, Long> payload) {
        try {
            userService.follow(payload.get("followerId"), payload.get("followingId"));
            return Result.success("关注成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消关注指定用户。
     *
     * @param payload 含 followerId、followingId 的请求体
     * @return 取消关注成功提示
     */
    @PostMapping("/unfollow")
    public Result<String> unfollow(@RequestBody java.util.Map<String, Long> payload) {
        try {
            userService.unfollow(payload.get("followerId"), payload.get("followingId"));
            return Result.success("取消关注成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询是否已关注某用户。
     *
     * @param followerId  关注者 ID
     * @param followingId 被关注者 ID
     * @return 是否已关注
     */
    @GetMapping("/is-following")
    public Result<Boolean> isFollowing(@RequestParam Long followerId, @RequestParam Long followingId) {
        return Result.success(userService.isFollowing(followerId, followingId));
    }

    /**
     * 查询用户关注的人列表。
     *
     * @param id 用户 ID
     * @return 关注用户列表
     */
    @GetMapping("/{id}/followings")
    public Result<List<User>> getFollowings(@PathVariable Long id) {
        return Result.success(userService.getFollowings(id));
    }

    /**
     * 查询用户的粉丝列表。
     *
     * @param id 用户 ID
     * @return 粉丝用户列表
     */
    @GetMapping("/{id}/followers")
    public Result<List<User>> getFollowers(@PathVariable Long id) {
        return Result.success(userService.getFollowers(id));
    }

    /**
     * 获取用户公开主页信息。
     *
     * @param id 用户 ID
     * @return 公开资料视图
     */
    @GetMapping("/{id}/public")
    public Result<com.example.fingerartbackend.dto.UserPublicProfile> getPublicProfile(@PathVariable Long id) {
        try {
            return Result.success(userService.getPublicProfile(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
