package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.ApplyUserPunishmentRequest;
import com.example.fingerartbackend.dto.UserPunishmentView;
import com.example.fingerartbackend.entity.UserPunishment;

import java.util.List;

/**
 * 用户服务接口，定义业务能力（业务服务接口）。
 */
public interface UserPunishmentService {

    List<UserPunishmentView> getActiveViews(Long userId);

    List<UserPunishment> applyPunishments(Long userId, Long adminId, ApplyUserPunishmentRequest request);

    void liftPunishment(Long userId, String type, Long adminId);

    void liftPunishments(Long userId, java.util.List<String> types, Long adminId);

    void liftAllPunishments(Long userId, Long adminId);

    boolean isPunished(Long userId, String type);

    boolean isAccountBanned(Long userId);

    void assertNotPunished(Long userId, String type, String message);
}
