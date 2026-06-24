package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.constant.UserPunishmentType;
import com.example.fingerartbackend.dto.ApplyUserPunishmentRequest;
import com.example.fingerartbackend.dto.UserPunishmentView;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.entity.UserPunishment;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.mapper.UserPunishmentMapper;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.UserPunishmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 用户服务实现类。
 */
@Service
public class UserPunishmentServiceImpl implements UserPunishmentService {

    private static final Set<String> VALID_TYPES = Set.of(
            UserPunishmentType.ACCOUNT_BAN,
            UserPunishmentType.NO_ORDER,
            UserPunishmentType.NO_FORUM,
            UserPunishmentType.NO_PRODUCT,
            UserPunishmentType.NO_SKILL
    );

    @Autowired
    private UserPunishmentMapper punishmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * 查询用户信息。
     */
    @Override
    public List<UserPunishmentView> getActiveViews(Long userId) {
        return punishmentMapper.findActiveByUserId(userId, LocalDateTime.now()).stream()
                .map(this::toView)
                .toList();
    }

    /**
     * 执行 applyPunishments 相关逻辑。
     */
    @Override
    @Transactional
    public List<UserPunishment> applyPunishments(Long userId, Long adminId, ApplyUserPunishmentRequest request) {
        userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (request.getTypes() == null || request.getTypes().isEmpty()) {
            throw new RuntimeException("请至少选择一种处罚类型");
        }
        boolean permanent = Boolean.TRUE.equals(request.getPermanent());
        Integer durationHours = request.getDurationHours();
        if (!permanent && (durationHours == null || durationHours <= 0)) {
            throw new RuntimeException("请设置处罚时长或选择永久处罚");
        }

        List<UserPunishment> saved = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = permanent ? null : now.plusHours(durationHours);

        for (String type : request.getTypes()) {
            if (type == null || !VALID_TYPES.contains(type)) {
                throw new RuntimeException("无效的处罚类型：" + type);
            }
            punishmentMapper.deleteByUserIdAndType(userId, type);

            UserPunishment punishment = new UserPunishment();
            punishment.setUserId(userId);
            punishment.setType(type);
            punishment.setStartAt(now);
            punishment.setEndAt(endAt);
            punishment.setReason(request.getReason());
            punishment.setAdminId(adminId);
            saved.add(punishmentMapper.save(punishment));

            notifyUser(userId, type, endAt, request.getReason());
        }
        return saved;
    }

    /**
     * 执行 liftPunishment 相关逻辑。
     */
    @Override
    @Transactional
    public void liftPunishment(Long userId, String type, Long adminId) {
        userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (type == null || !VALID_TYPES.contains(type)) {
            throw new RuntimeException("无效的处罚类型");
        }
        punishmentMapper.deleteByUserIdAndType(userId, type);
        notifyLift(userId, type);
    }

    /**
     * 执行 liftPunishments 相关逻辑。
     */
    @Override
    @Transactional
    public void liftPunishments(Long userId, List<String> types, Long adminId) {
        userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (types == null || types.isEmpty()) {
            throw new RuntimeException("请至少选择一种要解除的处罚");
        }
        for (String type : types) {
            if (type == null || !VALID_TYPES.contains(type)) {
                throw new RuntimeException("无效的处罚类型：" + type);
            }
            punishmentMapper.deleteByUserIdAndType(userId, type);
            notifyLift(userId, type);
        }
    }

    /**
     * 执行 liftAllPunishments 相关逻辑。
     */
    @Override
    @Transactional
    public void liftAllPunishments(Long userId, Long adminId) {
        userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        List<UserPunishment> active = punishmentMapper.findActiveByUserId(userId, LocalDateTime.now());
        if (active.isEmpty()) {
            return;
        }
        punishmentMapper.deleteAll(active);
        notificationService.notify(
                userId,
                "PUNISHMENT",
                "处罚已全部解除",
                "您的全部生效处罚已被管理员提前解除",
                "/account");
    }

    /**
     * 判断条件是否成立。
     */
    @Override
    public boolean isPunished(Long userId, String type) {
        if (userId == null || type == null) {
            return false;
        }
        return punishmentMapper.findActiveByUserId(userId, LocalDateTime.now()).stream()
                .anyMatch(p -> type.equals(p.getType()));
    }

    /**
     * 判断条件是否成立。
     */
    @Override
    public boolean isAccountBanned(Long userId) {
        return isPunished(userId, UserPunishmentType.ACCOUNT_BAN);
    }

    /**
     * 断言业务条件，不满足则抛异常。
     */
    @Override
    public void assertNotPunished(Long userId, String type, String message) {
        if (isPunished(userId, type)) {
            throw new RuntimeException(message != null ? message : "当前账号处于「" + UserPunishmentType.label(type) + "」处罚中");
        }
    }

    /**
     * 执行 toView 相关逻辑。
     */
    private UserPunishmentView toView(UserPunishment punishment) {
        UserPunishmentView view = new UserPunishmentView();
        view.setId(punishment.getId());
        view.setType(punishment.getType());
        view.setTypeLabel(UserPunishmentType.label(punishment.getType()));
        view.setStartAt(punishment.getStartAt());
        view.setEndAt(punishment.getEndAt());
        view.setReason(punishment.getReason());
        view.setPermanent(punishment.getEndAt() == null);
        return view;
    }

    /**
     * 发送通知。
     */
    private void notifyLift(Long userId, String type) {
        notificationService.notify(
                userId,
                "PUNISHMENT",
                "处罚已解除",
                "您的「" + UserPunishmentType.label(type) + "」处罚已被管理员提前解除",
                "/account");
    }

    /**
     * 发送通知。
     */
    private void notifyUser(Long userId, String type, LocalDateTime endAt, String reason) {
        String label = UserPunishmentType.label(type);
        String durationText = endAt == null
                ? "永久"
                : endAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CHINA));
        String detail = reason != null && !reason.isBlank() ? "。原因：" + reason.trim() : "";
        notificationService.notify(
                userId,
                "PUNISHMENT",
                "账号处罚通知",
                "您受到「" + label + "」处罚，将于 " + durationText + " 自动解除" + detail,
                "/account");
    }
}
