package com.example.fingerartbackend.config;

import com.example.fingerartbackend.service.CraftTechniqueService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CraftTechniqueDataInitializer implements ApplicationRunner {

    private final CraftTechniqueService craftTechniqueService;

    public CraftTechniqueDataInitializer(CraftTechniqueService craftTechniqueService) {
        this.craftTechniqueService = craftTechniqueService;
    }

    @Override
    public void run(ApplicationArguments args) {
        craftTechniqueService.initSampleData();
    }
}
