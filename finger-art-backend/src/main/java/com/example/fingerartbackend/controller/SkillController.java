package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/skills")
public class SkillController {
    @Autowired
    private SkillService skillService;

    @GetMapping
    public Result<List<Skill>> getAllSkills(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            return Result.success(skillService.getApprovedSkillsByUserId(userId));
        }
        if (category != null && !category.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                return Result.success(skillService.getApprovedSkillsByCategory(category)); // 简化：目前只有公共页传 status
            }
            return Result.success(skillService.getSkillsByCategory(category));
        }
        if (status != null && !status.isEmpty()) {
            return Result.success(skillService.getApprovedSkills());
        }
        return Result.success(skillService.getAllSkills());
    }

    @PatchMapping("/{id}/audit")
    public Result<Skill> auditSkill(@PathVariable Long id, @RequestParam String status) {
        try {
            return Result.success(skillService.auditSkill(id, status));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<Skill> addSkill(@RequestBody Skill skill) {
        return Result.success(skillService.saveSkill(skill));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return Result.success("技能删除成功");
    }
}