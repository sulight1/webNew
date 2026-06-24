package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.CraftTechnique;
import java.util.List;

/**
 * 工艺技法服务接口，定义业务能力（业务服务接口）。
 */
public interface CraftTechniqueService {
    List<CraftTechnique> getTechniquesByCategory(String category);
    List<CraftTechnique> getAllTechniques();
    List<CraftTechnique> initSampleData();
}
