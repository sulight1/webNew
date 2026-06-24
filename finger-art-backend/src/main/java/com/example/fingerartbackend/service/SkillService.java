package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.Skill;
import java.util.List;

/**
 * 技能服务接口，定义业务能力（业务服务接口）。
 */
public interface SkillService {
    List<Skill> getAllSkills();
    List<Skill> getApprovedSkills();
    List<Skill> getApprovedSkillsByCategory(String category);
    List<Skill> getSkillsByCategory(String category);
    Skill saveSkill(Skill skill);
    void deleteSkill(Long id);
    Skill auditSkill(Long id, String status);
    List<Skill> getApprovedSkillsByUserId(Long userId);
    List<Skill> getMySkills(Long userId);
    Skill updateSkill(Long id, Long userId, Skill patch);
    void deleteSkillByUser(Long id, Long userId);
}