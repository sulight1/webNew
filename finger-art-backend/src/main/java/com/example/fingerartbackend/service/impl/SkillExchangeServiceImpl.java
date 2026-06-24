package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.ScheduleSlot;
import com.example.fingerartbackend.entity.SkillExchange;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.ScheduleSlotMapper;
import com.example.fingerartbackend.mapper.SkillExchangeRepository;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.CoinEconomyService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.SkillExchangeService;
import com.example.fingerartbackend.service.UserPunishmentService;
import com.example.fingerartbackend.constant.UserPunishmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 技能服务实现类。
 */
@Service
public class SkillExchangeServiceImpl implements SkillExchangeService {

    @Autowired
    private SkillExchangeRepository exchangeRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ScheduleSlotMapper scheduleSlotMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CoinEconomyService coinEconomyService;

    @Autowired
    private UserPunishmentService userPunishmentService;

    /**
     * 执行 requestExchange 相关逻辑。
     */
    @Override
    @Transactional
    public SkillExchange requestExchange(Long userAId, Long userBId, String description, Integer cost, String scheduleDateStr) {
        userPunishmentService.assertNotPunished(userAId, UserPunishmentType.NO_SKILL, "您已被禁止发布和交换技能");
        if (userAId != null && userAId.equals(userBId)) {
            throw new RuntimeException("不能与自己交换技能");
        }
        User userA = userMapper.findById(userAId).orElseThrow(() -> new RuntimeException("User A not found"));
        User userB = userMapper.findById(userBId).orElseThrow(() -> new RuntimeException("User B not found"));

        SkillExchange exchange = new SkillExchange();
        exchange.setUserA(userA);
        exchange.setUserB(userB);
        exchange.setDescription(description);
        exchange.setZaowuBiCost(cost);
        exchange.setStatus("REQUESTED");
        exchange.setUserAConfirmed(false);
        exchange.setUserBConfirmed(false);
        exchange.setUserAReviewed(false);
        exchange.setUserBReviewed(false);

        if (scheduleDateStr != null && !scheduleDateStr.isEmpty()) {
            LocalDate scheduleDate = LocalDate.parse(scheduleDateStr);
            exchange.setScheduleDate(scheduleDate);
            markProviderSlot(userBId, scheduleDate, "PENDING", "交换预约: " + description);
        }

        SkillExchange saved = exchangeRepository.save(exchange);
        notificationService.notify(
                userBId,
                "EXCHANGE_CONFIRM",
                "收到新的技能交换请求",
                userA.getUsername() + " 向你发起了技能交换，请确认是否接单",
                "/skill-exchange"
        );
        return saved;
    }

    /**
     * 执行 acceptExchange 相关逻辑。
     */
    @Override
    @Transactional
    public SkillExchange acceptExchange(Long exchangeId, Long userId) {
        SkillExchange exchange = getExchange(exchangeId);
        if (!userId.equals(exchange.getUserB().getId())) {
            throw new RuntimeException("仅技能提供方可接受请求");
        }
        if (!"REQUESTED".equals(exchange.getStatus())) {
            throw new RuntimeException("当前状态不可接受");
        }
        exchange.setStatus("ACCEPTED");
        SkillExchange saved = exchangeRepository.save(exchange);
        notificationService.notify(
                exchange.getUserA().getId(),
                "EXCHANGE_CONFIRM",
                "对方已接受交换请求",
                exchange.getUserB().getUsername() + " 已接受你的交换请求，请确认预约时间",
                "/skill-exchange"
        );
        return saved;
    }

    /**
     * 确认操作。
     */
    @Override
    @Transactional
    public SkillExchange confirmExchange(Long exchangeId, Long userId) {
        SkillExchange exchange = getExchange(exchangeId);
        if (!Arrays.asList("ACCEPTED", "CONFIRMED").contains(exchange.getStatus())) {
            throw new RuntimeException("当前状态不可确认预约");
        }
        if (userId.equals(exchange.getUserA().getId())) {
            exchange.setUserAConfirmed(true);
        } else if (userId.equals(exchange.getUserB().getId())) {
            exchange.setUserBConfirmed(true);
        } else {
            throw new RuntimeException("无权确认该交换");
        }

        boolean bothConfirmed = Boolean.TRUE.equals(exchange.getUserAConfirmed())
                && Boolean.TRUE.equals(exchange.getUserBConfirmed());
        if (bothConfirmed) {
            exchange.setStatus("CONFIRMED");
            if (exchange.getScheduleDate() != null) {
                markProviderSlot(exchange.getUserB().getId(), exchange.getScheduleDate(), "BUSY", "已确认交换 #" + exchange.getId());
            }
            notificationService.notify(
                    exchange.getUserA().getId(),
                    "EXCHANGE_CONFIRM",
                    "预约已双方确认",
                    "你与 " + exchange.getUserB().getUsername() + " 的技能交换预约已锁定",
                    "/skill-exchange"
            );
            notificationService.notify(
                    exchange.getUserB().getId(),
                    "EXCHANGE_CONFIRM",
                    "预约已双方确认",
                    "你与 " + exchange.getUserA().getUsername() + " 的技能交换预约已锁定",
                    "/skill-exchange"
            );
        } else {
            exchange.setStatus("ACCEPTED");
            Long otherId = userId.equals(exchange.getUserA().getId())
                    ? exchange.getUserB().getId() : exchange.getUserA().getId();
            notificationService.notify(
                    otherId,
                    "EXCHANGE_CONFIRM",
                    "对方已确认预约",
                    "请尽快确认技能交换预约时间",
                    "/skill-exchange"
            );
        }
        return exchangeRepository.save(exchange);
    }

    /**
     * 完成技能。
     */
    @Override
    @Transactional
    public SkillExchange completeExchange(Long exchangeId, Long userId) {
        SkillExchange exchange = getExchange(exchangeId);
        if (!"CONFIRMED".equals(exchange.getStatus())) {
            throw new RuntimeException("双方确认预约后才可完成交换");
        }
        if (!userId.equals(exchange.getUserB().getId())) {
            throw new RuntimeException("仅技能提供方可确认完成并收款");
        }
        if ("COMPLETED".equals(exchange.getStatus())) {
            throw new RuntimeException("Exchange already completed");
        }

        User userA = exchange.getUserA();
        User userB = exchange.getUserB();
        Integer cost = exchange.getZaowuBiCost() != null ? exchange.getZaowuBiCost() : 10;

        if (userA.getZaowuBiBalance() < cost) {
            throw new RuntimeException("Insufficient balance");
        }
        userA.setZaowuBiBalance(userA.getZaowuBiBalance() - cost);
        userB.setZaowuBiBalance((userB.getZaowuBiBalance() != null ? userB.getZaowuBiBalance() : 0.0) + cost);
        userMapper.save(userA);
        userMapper.save(userB);

        bumpCredit(userA);
        bumpCredit(userB);

        exchange.setStatus("COMPLETED");
        SkillExchange saved = exchangeRepository.save(exchange);
        notificationService.notify(
                exchange.getUserA().getId(),
                "EXCHANGE_CONFIRM",
                "技能交换已完成",
                "请为本次交换体验评价 " + exchange.getUserB().getUsername(),
                "/skill-exchange"
        );
        notificationService.notify(
                exchange.getUserB().getId(),
                "EXCHANGE_CONFIRM",
                "技能交换已完成",
                "请为本次交换体验评价 " + exchange.getUserA().getUsername(),
                "/skill-exchange"
        );
        coinEconomyService.grantEventReward(exchange.getUserA().getId(), "exchange_complete", exchangeId, 15, "完成技能交换");
        coinEconomyService.grantEventReward(exchange.getUserB().getId(), "exchange_complete", exchangeId, 15, "完成技能交换");
        return saved;
    }

    /**
     * 执行 reportNoShow 相关逻辑。
     */
    @Override
    @Transactional
    public SkillExchange reportNoShow(Long exchangeId, Long reporterId) {
        SkillExchange exchange = getExchange(exchangeId);
        if (!Arrays.asList("ACCEPTED", "CONFIRMED").contains(exchange.getStatus())) {
            throw new RuntimeException("当前状态不可标记爽约");
        }
        if (exchange.getScheduleDate() == null || !exchange.getScheduleDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("预约日当天之后才可标记爽约");
        }
        if (!reporterId.equals(exchange.getUserA().getId()) && !reporterId.equals(exchange.getUserB().getId())) {
            throw new RuntimeException("无权操作该交换");
        }

        User offender = reporterId.equals(exchange.getUserA().getId())
                ? exchange.getUserB() : exchange.getUserA();
        penalizeCredit(offender, 10);

        if (exchange.getScheduleDate() != null) {
            markProviderSlot(exchange.getUserB().getId(), exchange.getScheduleDate(), "FREE", "爽约释放 #" + exchange.getId());
        }
        exchange.setStatus("NO_SHOW");
        SkillExchange saved = exchangeRepository.save(exchange);
        notificationService.notify(
                offender.getId(),
                "EXCHANGE_NO_SHOW",
                "技能交换被标记为爽约",
                "本次预约未履约，信用分已扣减 10 分",
                "/skill-exchange"
        );
        return saved;
    }

    /**
     * 查询技能信息。
     */
    @Override
    public List<SkillExchange> getMyExchanges(Long userId) {
        User user = userMapper.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        processOverdueExchanges();
        return exchangeRepository.findByUser(user);
    }

    /**
     * 执行 processOverdueExchanges 相关逻辑。
     */
    @Override
    @Transactional
    @Scheduled(cron = "0 0 8 * * ?")
    public void processOverdueExchanges() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<SkillExchange> overdue = exchangeRepository.findByStatusInAndScheduleDateBefore(
                Arrays.asList("CONFIRMED", "ACCEPTED"), yesterday);
        for (SkillExchange exchange : overdue) {
            if ("COMPLETED".equals(exchange.getStatus()) || "NO_SHOW".equals(exchange.getStatus())) {
                continue;
            }
            User offender = exchange.getUserA();
            penalizeCredit(offender, 5);
            if (exchange.getScheduleDate() != null) {
                markProviderSlot(exchange.getUserB().getId(), exchange.getScheduleDate(), "FREE", "逾期未完成 #" + exchange.getId());
            }
            exchange.setStatus("NO_SHOW");
            exchangeRepository.save(exchange);
            notificationService.notify(
                    exchange.getUserA().getId(),
                    "EXCHANGE_NO_SHOW",
                    "交换预约逾期未履约",
                    "系统已自动标记爽约并扣减信用分",
                    "/skill-exchange"
            );
            notificationService.notify(
                    exchange.getUserB().getId(),
                    "EXCHANGE_NO_SHOW",
                    "交换预约逾期未履约",
                    "对方未按时完成交换，相关排期已释放",
                    "/skill-exchange"
            );
        }
    }

    /**
     * 查询技能信息。
     */
    private SkillExchange getExchange(Long exchangeId) {
        return exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange not found"));
    }

    /**
     * 执行 markProviderSlot 相关逻辑。
     */
    private void markProviderSlot(Long userId, LocalDate date, String status, String remark) {
        List<ScheduleSlot> slots = scheduleSlotMapper.findByUserIdAndDate(userId, date);
        ScheduleSlot slot = slots.isEmpty() ? new ScheduleSlot() : slots.get(0);
        slot.setUserId(userId);
        slot.setDate(date);
        slot.setStatus(status);
        slot.setRemark(remark);
        scheduleSlotMapper.save(slot);
    }

    /**
     * 执行 bumpCredit 相关逻辑。
     */
    private void bumpCredit(User user) {
        user.setCreditScore((user.getCreditScore() != null ? user.getCreditScore() : 100) + 1);
        user.setCompletedOrders((user.getCompletedOrders() != null ? user.getCompletedOrders() : 0) + 1);
        userMapper.save(user);
    }

    /**
     * 执行 penalizeCredit 相关逻辑。
     */
    private void penalizeCredit(User user, int amount) {
        int credit = user.getCreditScore() != null ? user.getCreditScore() : 100;
        user.setCreditScore(Math.max(0, credit - amount));
        userMapper.save(user);
    }
}
