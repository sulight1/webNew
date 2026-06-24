package com.example.fingerartbackend.config;

import com.example.fingerartbackend.service.CraftTechniqueService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 手工艺技法示例数据初始化器。
 * <p>
 * 应用启动后自动调用 {@link CraftTechniqueService#initSampleData()}，
 * 在技法库为空时写入预设的工艺百科数据。
 * </p>
 */
@Component
public class CraftTechniqueDataInitializer implements ApplicationRunner {

    private final CraftTechniqueService craftTechniqueService;

    public CraftTechniqueDataInitializer(CraftTechniqueService craftTechniqueService) {
        this.craftTechniqueService = craftTechniqueService;
    }

    /**
     * 执行 run 相关逻辑。
     */
    @Override
    public void run(ApplicationArguments args) {
        craftTechniqueService.initSampleData();
    }
}
