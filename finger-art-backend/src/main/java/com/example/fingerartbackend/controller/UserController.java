package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.dto.LoginRequest;
import com.example.fingerartbackend.dto.LoginResponse;
import com.example.fingerartbackend.dto.RegisterRequest;
import com.example.fingerartbackend.auth.LoginResponseFactory;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.service.UserService;
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
            return Result.success(loginResponseFactory.build(loggedIn));
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
            return Result.success(userService.approveArtisan(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/artisan-applications/{id}/reject")
    public Result<User> rejectArtisan(@PathVariable Long id) {
        try {
            return Result.success(userService.rejectArtisan(id));
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
            return Result.success(userService.addZaoWuBi(userId, amount));
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
        return Result.success(userService.getAllUsers());
    }

    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        try {
            String password = payload.get("password");
            userService.resetPassword(id, password);
            return Result.success("密码已重置");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户删除成功");
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
