package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.CraftTechnique;
import java.util.List;

public interface CraftTechniqueService {
    List<CraftTechnique> getTechniquesByCategory(String category);
    List<CraftTechnique> getAllTechniques();
    List<CraftTechnique> initSampleData();
}
