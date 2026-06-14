package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.Skill;
import java.util.List;

public interface SkillService {
    List<Skill> getAllSkills();
    List<Skill> getApprovedSkills();
    List<Skill> getApprovedSkillsByCategory(String category);
    List<Skill> getSkillsByCategory(String category);
    Skill saveSkill(Skill skill);
    void deleteSkill(Long id);
    Skill auditSkill(Long id, String status);
    List<Skill> getApprovedSkillsByUserId(Long userId);
}