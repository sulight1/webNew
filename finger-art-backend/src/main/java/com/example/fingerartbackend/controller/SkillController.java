package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.service.AdminAuditService;
import com.example.fingerartbackend.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 技能发布控制器。
 * 负责技能的查询、发布、审核与删除，对应技能交换模块。
 */
@RestController
@RequestMapping("/skills")
public class SkillController {
    @Autowired
    private SkillService skillService;

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 查询技能列表，支持按分类、状态、用户筛选。
     *
     * @param category 可选技能分类
     * @param status   可选审核状态
     * @param userId   可选发布者用户 ID
     * @return 符合条件的技能列表
     */
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

    /**
     * 管理员审核技能发布。
     *
     * @param id     技能 ID
     * @param status 审核结果状态
     * @return 审核后的技能实体
     */
    @PatchMapping("/{id}/audit")
    public Result<Skill> auditSkill(@PathVariable Long id, @RequestParam String status) {
        try {
            Skill skill = skillService.auditSkill(id, status);
            adminAuditService.log("AUDIT_SKILL", "SKILL", id,
                    "审核技能「" + skill.getTitle() + "」为 " + status);
            return Result.success(skill);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布新技能。
     *
     * @param skill 技能实体
     * @return 保存后的技能
     */
    @PostMapping
    public Result<Skill> addSkill(@RequestBody Skill skill) {
        return Result.success(skillService.saveSkill(skill));
    }

    /**
     * 删除指定技能。
     *
     * @param id 技能 ID
     * @return 删除成功提示
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return Result.success("技能删除成功");
    }
}
