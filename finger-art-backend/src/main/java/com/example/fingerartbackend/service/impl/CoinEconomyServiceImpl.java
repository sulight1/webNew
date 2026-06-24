package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.CoinTaskClaim;
import com.example.fingerartbackend.entity.Product;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.CoinTaskClaimMapper;
import com.example.fingerartbackend.mapper.ProductMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.CoinEconomyService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 造物币经济服务实现类。
 */
@Service
public class CoinEconomyServiceImpl implements CoinEconomyService {

    private static final int CHECKIN_COINS = 5;
    private static final int BROWSE_COINS = 2;
    private static final int PUBLISH_COINS = 8;
    private static final int BOOST_COST = 20;
    private static final int BOOST_WEIGHT = 100;
    private static final int BOOST_DAYS = 7;

    @Autowired
    private CoinTaskClaimMapper claimMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;

    /**
     * 执行 checkIn 相关逻辑。
     */
    @Override
    @Transactional
    public Map<String, Object> checkIn(Long userId) {
        return claimDailyInternal(userId, "daily_checkin", CHECKIN_COINS, "每日签到");
    }

    /**
     * 查询造物币经济信息。
     */
    @Override
    public List<Map<String, Object>> getTaskStatus(Long userId) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(taskRow("daily_checkin", "每日签到", CHECKIN_COINS, "登录即可领取", claimMapper.existsByUserIdAndTaskCodeAndClaimDate(userId, "daily_checkin", today)));
        tasks.add(taskRow("daily_browse", "逛市集", BROWSE_COINS, "浏览造物市集后领取", claimMapper.existsByUserIdAndTaskCodeAndClaimDate(userId, "daily_browse", today)));
        tasks.add(taskRow("daily_publish", "发布灵感", PUBLISH_COINS, "发布或更新作品后领取", claimMapper.existsByUserIdAndTaskCodeAndClaimDate(userId, "daily_publish", today)));
        return tasks;
    }

    /**
     * 执行 claimDailyTask 相关逻辑。
     */
    @Override
    @Transactional
    public Map<String, Object> claimDailyTask(Long userId, String taskCode) {
        return switch (taskCode) {
            case "daily_checkin" -> claimDailyInternal(userId, taskCode, CHECKIN_COINS, "每日签到");
            case "daily_browse" -> claimDailyInternal(userId, taskCode, BROWSE_COINS, "逛市集");
            case "daily_publish" -> claimDailyInternal(userId, taskCode, PUBLISH_COINS, "发布灵感");
            default -> throw new RuntimeException("未知任务：" + taskCode);
        };
    }

    /**
     * 执行 grantEventReward 相关逻辑。
     */
    @Override
    @Transactional
    public void grantEventReward(Long userId, String taskCode, Long referenceId, int coins, String title) {
        if (claimMapper.existsByUserIdAndTaskCodeAndReferenceId(userId, taskCode, referenceId)) {
            return;
        }
        userService.addZaoWuBi(userId, (double) coins);
        CoinTaskClaim claim = new CoinTaskClaim();
        claim.setUserId(userId);
        claim.setTaskCode(taskCode);
        claim.setReferenceId(referenceId);
        claim.setCoinsGranted(coins);
        claimMapper.save(claim);
        notificationService.notify(userId, "COIN", "造物币奖励", title + " +" + coins + " 币", "/artisan-dashboard?menu=coin-tasks");
    }

    /**
     * 执行 boostProductExposure 相关逻辑。
     */
    @Override
    @Transactional
    public Product boostProductExposure(Long userId, Long productId) {
        User user = userMapper.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Product product = productMapper.findById(productId).orElseThrow(() -> new RuntimeException("作品不存在"));
        if (!userId.equals(product.getCreatorId())) {
            throw new RuntimeException("只能推广自己的作品");
        }
        double balance = user.getZaowuBiBalance() != null ? user.getZaowuBiBalance() : 0;
        if (balance < BOOST_COST) {
            throw new RuntimeException("造物币不足，推广需要 " + BOOST_COST + " 币");
        }
        userService.addZaoWuBi(userId, (double) -BOOST_COST);
        product.setExposureBoost(BOOST_WEIGHT);
        product.setBoostUntil(LocalDateTime.now().plusDays(BOOST_DAYS));
        return productMapper.save(product);
    }

    /**
     * 执行 claimDailyInternal 相关逻辑。
     */
    private Map<String, Object> claimDailyInternal(Long userId, String taskCode, int coins, String title) {
        LocalDate today = LocalDate.now();
        if (claimMapper.existsByUserIdAndTaskCodeAndClaimDate(userId, taskCode, today)) {
            throw new RuntimeException("今日已领取该奖励");
        }
        User updated = userService.addZaoWuBi(userId, (double) coins);
        CoinTaskClaim claim = new CoinTaskClaim();
        claim.setUserId(userId);
        claim.setTaskCode(taskCode);
        claim.setClaimDate(today);
        claim.setCoinsGranted(coins);
        claimMapper.save(claim);

        Map<String, Object> result = new HashMap<>();
        result.put("coins", coins);
        result.put("balance", updated.getZaowuBiBalance());
        result.put("message", title + "成功，+" + coins + " 造物币");
        return result;
    }

    /**
     * 执行 taskRow 相关逻辑。
     */
    private Map<String, Object> taskRow(String code, String name, int coins, String desc, boolean claimed) {
        Map<String, Object> row = new HashMap<>();
        row.put("code", code);
        row.put("name", name);
        row.put("coins", coins);
        row.put("description", desc);
        row.put("claimed", claimed);
        return row;
    }
}
