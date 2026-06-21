package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.dto.RegisterRequest;
import com.example.fingerartbackend.dto.UserPublicProfile;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.entity.Follow;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.mapper.SkillMapper;
import com.example.fingerartbackend.mapper.ProductMapper;
import com.example.fingerartbackend.mapper.FollowMapper;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.PasswordService;
import com.example.fingerartbackend.service.UserService;
import com.example.fingerartbackend.service.UserPunishmentService;
import com.example.fingerartbackend.util.AccountUtils;
import com.example.fingerartbackend.util.NicknameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserPunishmentService userPunishmentService;

    @Override
    public User register(RegisterRequest request) {
        if (request == null) {
            throw new RuntimeException("注册信息不能为空");
        }
        String account = request.getAccount() != null ? request.getAccount().trim() : null;
        AccountUtils.validateNumericAccount(account);
        if (userMapper.existsByAccount(account)) {
            throw new RuntimeException("该账号已被注册");
        }
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();
        if (password == null || password.isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new RuntimeException("两次密码不一致");
        }
        if (password.length() < 6) {
            throw new RuntimeException("密码至少 6 位");
        }

        User user = new User();
        user.setAccount(account);
        user.setUsername(NicknameGenerator.randomNickname());
        user.setPassword(passwordService.encode(password));
        user.setRole("BUYER");
        user.setArtisanApplyStatus("NONE");
        return userMapper.save(user);
    }

    @Override
    @Transactional
    public User applyArtisan(Long userId) {
        User user = getUserById(userId);
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("管理员无需申请手作达人");
        }
        if ("ARTISAN".equals(user.getRole())) {
            return user;
        }
        if ("PENDING".equals(user.getArtisanApplyStatus())) {
            throw new RuntimeException("你的申请正在审核中，请耐心等待");
        }
        user.setArtisanApplyStatus("PENDING");
        User saved = userMapper.save(user);
        userMapper.findAll().stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .forEach(admin -> notificationService.notify(
                        admin.getId(),
                        "ARTISAN_APPLY",
                        "新手作达人申请",
                        user.getUsername() + " 申请成为手作达人，请前往用户管理审核",
                        "/admin"));
        return saved;
    }

    @Override
    @Transactional
    public User approveArtisan(Long userId) {
        User user = getUserById(userId);
        if (!"PENDING".equals(user.getArtisanApplyStatus())) {
            throw new RuntimeException("该用户没有待审核的手作达人申请");
        }
        user.setRole("ARTISAN");
        user.setArtisanApplyStatus("APPROVED");
        User saved = userMapper.save(user);
        notificationService.notify(userId, "ARTISAN_APPLY", "手作达人审核通过",
                "恭喜！你已成为认证手作达人，快去工作台发布作品吧", "/studio");
        return saved;
    }

    @Override
    @Transactional
    public User rejectArtisan(Long userId) {
        User user = getUserById(userId);
        if (!"PENDING".equals(user.getArtisanApplyStatus())) {
            throw new RuntimeException("该用户没有待审核的手作达人申请");
        }
        user.setArtisanApplyStatus("REJECTED");
        User saved = userMapper.save(user);
        notificationService.notify(userId, "ARTISAN_APPLY", "手作达人审核未通过",
                "你的手作达人申请未通过，可完善资料后重新申请", "/account?menu=apply-artisan");
        return saved;
    }

    @Override
    public List<User> listPendingArtisanApplications() {
        return userMapper.findAll().stream()
                .filter(u -> "PENDING".equals(u.getArtisanApplyStatus()))
                .collect(Collectors.toList());
    }

    private void normalizeRole(User user) {
        if (user.getArtisanApplyStatus() == null || user.getArtisanApplyStatus().isBlank()) {
            user.setArtisanApplyStatus("ARTISAN".equals(user.getRole()) ? "APPROVED" : "NONE");
        }
        if (!"ARTISAN".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            if (user.getRole() == null || user.getRole().isBlank()
                    || "USER".equals(user.getRole()) || "CREATOR".equals(user.getRole())) {
                user.setRole("BUYER");
                userMapper.save(user);
            }
        }
    }

    @Override
    public User login(String account, String password) {
        if (account == null || account.isBlank()) {
            throw new RuntimeException("请输入账号");
        }
        User user = userMapper.findByAccount(account.trim())
                .orElseThrow(() -> new RuntimeException("账号不存在"));

        if (password == null || password.isBlank()) {
            throw new RuntimeException("请输入密码");
        }
        
        if (!passwordService.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (passwordService.needsUpgrade(user.getPassword())) {
            user.setPassword(passwordService.encode(password));
            userMapper.save(user);
        }
        if (userPunishmentService.isAccountBanned(user.getId())) {
            throw new RuntimeException("账号已被封禁，如有疑问请联系平台");
        }
        normalizeRole(user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existing = userMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getAccount() != null && existing.getAccount() != null
                && !user.getAccount().equals(existing.getAccount())) {
            throw new RuntimeException("账号不可修改");
        }
        
        String oldUsername = existing.getUsername();
        boolean usernameChanged = false;
        
        // 昵称可修改，账号不可改
        if (user.getUsername() != null && !user.getUsername().isBlank()
                && !user.getUsername().equals(oldUsername)) {
            existing.setUsername(user.getUsername().trim());
            usernameChanged = true;
        }
        
        if (user.getAvatar() != null) {
            existing.setAvatar(user.getAvatar());
        }
        
        if (user.getEmail() != null) {
            existing.setEmail(user.getEmail());
        }

        if (user.getBio() != null) {
            existing.setBio(user.getBio());
        }

        if (user.getShippingName() != null) {
            existing.setShippingName(user.getShippingName().trim());
        }
        if (user.getShippingPhone() != null) {
            existing.setShippingPhone(user.getShippingPhone().trim());
        }
        if (user.getShippingAddress() != null) {
            existing.setShippingAddress(user.getShippingAddress().trim());
        }

        if (user.getRole() != null) {
            existing.setRole(user.getRole());
        }
        if (user.getCreditScore() != null) {
            existing.setCreditScore(user.getCreditScore());
        }
        
        User updatedUser = userMapper.save(existing);

        // 同步更新 Skill 表中的冗余数据
        List<Skill> userSkills = skillMapper.findByUserId(id);
        for (Skill s : userSkills) {
            s.setUsername(updatedUser.getUsername());
            s.setAvatar(updatedUser.getAvatar());
            skillMapper.save(s);
        }

        // 同步更新 Product 表中的冗余数据
        // 尝试通过 ID 查找，如果没有 ID 则通过旧用户名查找（兼容旧数据）
        List<Product> userProducts = productMapper.findByCreatorId(id);
        if (userProducts.isEmpty()) {
            userProducts = productMapper.findByCreator(oldUsername);
        }
        for (Product p : userProducts) {
            p.setCreatorId(id);
            p.setCreator(updatedUser.getUsername());
            p.setCreatorAvatar(updatedUser.getAvatar());
            productMapper.save(p);
        }
        
        return updatedUser;
    }

    @Override
    public User resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("密码至少 6 位");
        }
        User existing = userMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        existing.setPassword(passwordService.encode(newPassword));
        existing.setPasswordResetStatus("NONE");
        return userMapper.save(existing);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String account) {
        if (account == null || account.isBlank()) {
            throw new RuntimeException("请输入账号");
        }
        User user = userMapper.findByAccount(account.trim())
                .orElse(null);
        if (user == null || "ADMIN".equals(user.getRole())) {
            return;
        }
        user.setPasswordResetStatus("PENDING");
        userMapper.save(user);
        userMapper.findAll().stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .forEach(admin -> notificationService.notify(
                        admin.getId(),
                        "PASSWORD_RESET",
                        "密码重置申请",
                        user.getUsername() + "（账号 " + user.getAccount() + "）申请重置密码，请前往用户管理处理",
                        "/admin"));
    }

    @Override
    public User addZaoWuBi(Long userId, Double amount) {
        User user = getUserById(userId);
        Double currentBalance = user.getZaowuBiBalance() != null ? user.getZaowuBiBalance() : 0.0;
        double newBalance = currentBalance + amount;
        if (newBalance < 0) {
            throw new RuntimeException("造物币余额不足，无法扣减");
        }
        user.setZaowuBiBalance(newBalance);
        return userMapper.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }
        if (followMapper.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return;
        }
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        followMapper.save(follow);
        userMapper.findById(followerId).ifPresent(follower -> notificationService.notify(
                followingId,
                "FOLLOW",
                "新粉丝",
                follower.getUsername() + " 关注了你",
                "/artisan-dashboard?menu=followers"));
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        followMapper.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        return followMapper.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public List<User> getFollowers(Long userId) {
        List<Follow> follows = followMapper.findByFollowingId(userId);
        return follows.stream()
                .map(f -> userMapper.findById(f.getFollowerId()).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getFollowings(Long userId) {
        List<Follow> follows = followMapper.findByFollowerId(userId);
        return follows.stream()
                .map(f -> userMapper.findById(f.getFollowingId()).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> listTopArtisans(int limit, Long excludeUserId) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return userMapper.findByRole("ARTISAN").stream()
                .filter(u -> excludeUserId == null || !excludeUserId.equals(u.getId()))
                .sorted((a, b) -> {
                    int creditCmp = Integer.compare(
                            b.getCreditScore() != null ? b.getCreditScore() : 0,
                            a.getCreditScore() != null ? a.getCreditScore() : 0);
                    if (creditCmp != 0) {
                        return creditCmp;
                    }
                    return Double.compare(
                            b.getRating() != null ? b.getRating() : 0,
                            a.getRating() != null ? a.getRating() : 0);
                })
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    @Override
    public UserPublicProfile getPublicProfile(Long id) {
        User user = getUserById(id);
        UserPublicProfile profile = new UserPublicProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setRole(user.getRole());
        profile.setAvatar(user.getAvatar());
        profile.setBio(user.getBio());
        profile.setRating(user.getRating());
        profile.setReviewCount(user.getReviewCount());
        profile.setCompletedOrders(user.getCompletedOrders());
        profile.setCreditScore(user.getCreditScore());
        profile.setFollowerCount(followMapper.findByFollowingId(id).size());
        profile.setFollowingCount(followMapper.findByFollowerId(id).size());
        profile.setProductCount(productMapper.findByCreatorId(id).stream()
                .filter(p -> "APPROVED".equals(p.getStatus())).count());
        profile.setSkillCount(skillMapper.findByUserId(id).stream()
                .filter(s -> "APPROVED".equals(s.getStatus())).count());
        return profile;
    }
}