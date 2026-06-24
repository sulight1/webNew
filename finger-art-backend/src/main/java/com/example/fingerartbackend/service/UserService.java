package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.RegisterRequest;
import com.example.fingerartbackend.entity.User;
import java.util.List;

/**
 * 用户服务接口，定义业务能力（业务服务接口）。
 */
public interface UserService {
    User register(RegisterRequest request);
    User applyArtisan(Long userId);
    User approveArtisan(Long userId);
    User rejectArtisan(Long userId);
    List<User> listPendingArtisanApplications();
    User login(String account, String password);
    List<User> getAllUsers();
    void deleteUser(Long id);
    User updateUser(Long id, User user);
    User resetPassword(Long id, String newPassword);
    void requestPasswordReset(String account);
    User addZaoWuBi(Long userId, Double amount);
    User getUserById(Long id);
    
    // 关注功能
    void follow(Long followerId, Long followingId);
    void unfollow(Long followerId, Long followingId);
    boolean isFollowing(Long followerId, Long followingId);
    List<User> getFollowers(Long userId);
    List<User> getFollowings(Long userId);

    com.example.fingerartbackend.dto.UserPublicProfile getPublicProfile(Long id);

    List<User> listTopArtisans(int limit, Long excludeUserId);
}