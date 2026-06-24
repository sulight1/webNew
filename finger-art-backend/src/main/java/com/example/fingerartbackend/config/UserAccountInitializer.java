package com.example.fingerartbackend.config;

import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.util.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 用户数字账号补全初始化器。
 * <p>
 * 应用启动时扫描全部用户，为缺少 {@code account} 字段的历史用户
 * 按用户 ID 生成固定格式的数字账号（10000001 起）。
 * </p>
 */
@Component
@Order(1)
public class UserAccountInitializer implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;

    /**
     * 执行 run 相关逻辑。
     */
    @Override
    public void run(ApplicationArguments args) {
        for (User user : userMapper.findAll()) {
            if (user.getAccount() == null || user.getAccount().isBlank()) {
                user.setAccount(AccountUtils.fixedAccountForUserId(user.getId()));
                userMapper.save(user);
            }
        }
    }
}
