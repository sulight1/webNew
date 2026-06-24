package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.CraftTechnique;
import com.example.fingerartbackend.service.CraftTechniqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工艺技法控制器。
 * 提供手作工艺技法字典的查询，对应作品分类与工艺标签模块。
 */
@RestController
@RequestMapping("/craft-techniques")
public class CraftTechniqueController {

    @Autowired
    private CraftTechniqueService craftTechniqueService;

    /**
     * 查询工艺技法列表，可按作品分类筛选。
     *
     * @param category 可选作品分类，为空时返回全部技法
     * @return 工艺技法列表
     */
    @GetMapping
    public Result<List<CraftTechnique>> getTechniques(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return Result.success(craftTechniqueService.getTechniquesByCategory(category));
        }
        return Result.success(craftTechniqueService.getAllTechniques());
    }
}
