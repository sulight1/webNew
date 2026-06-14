package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.CraftTechnique;
import com.example.fingerartbackend.service.CraftTechniqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/craft-techniques")
public class CraftTechniqueController {

    @Autowired
    private CraftTechniqueService craftTechniqueService;

    @GetMapping
    public Result<List<CraftTechnique>> getTechniques(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return Result.success(craftTechniqueService.getTechniquesByCategory(category));
        }
        return Result.success(craftTechniqueService.getAllTechniques());
    }
}
