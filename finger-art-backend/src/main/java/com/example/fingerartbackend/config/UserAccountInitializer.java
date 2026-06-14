package com.example.fingerartbackend.config;

import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.util.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class UserAccountInitializer implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;

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
