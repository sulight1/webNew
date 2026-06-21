package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.mapper.SkillMapper;
import com.example.fingerartbackend.service.SkillService;
import com.example.fingerartbackend.service.UserPunishmentService;
import com.example.fingerartbackend.constant.UserPunishmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class SkillServiceImpl implements SkillService {
    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private UserPunishmentService userPunishmentService;

    @Override
    public List<Skill> getAllSkills() {
        // 一次性清理逻辑：删除数据库中遗留的假数据
        List<String> fakeUsernames = Arrays.asList("织织酱", "摄影师阿木", "喵喵美甲");
        List<Skill> allSkills = skillMapper.findAll();
        List<Skill> fakeSkills = allSkills.stream()
                .filter(s -> fakeUsernames.contains(s.getUsername()))
                .collect(java.util.stream.Collectors.toList());
        
        if (!fakeSkills.isEmpty()) {
            skillMapper.deleteAll(fakeSkills);
            return skillMapper.findAll(); // 重新获取清理后的列表
        }
        
        return allSkills;
    }

    @Override
    public List<Skill> getApprovedSkills() {
        return skillMapper.findByStatus("APPROVED");
    }

    @Override
    public List<Skill> getApprovedSkillsByCategory(String category) {
        return skillMapper.findByCategoryAndStatus(category, "APPROVED");
    }

    @Override
    public List<Skill> getSkillsByCategory(String category) {
        return skillMapper.findByCategory(category);
    }

    @Override
    public Skill saveSkill(Skill skill) {
        if (skill.getUserId() != null) {
            userPunishmentService.assertNotPunished(skill.getUserId(), UserPunishmentType.NO_SKILL, "您已被禁止发布和交换技能");
        }
        if (skill.getRating() == null) skill.setRating(5.0);
        if (skill.getCredit() == null) skill.setCredit(100);
        if (skill.getExchangeCount() == null) skill.setExchangeCount(0);
        if (skill.getStatus() == null) skill.setStatus("PENDING");
        return skillMapper.save(skill);
    }

    @Override
    public void deleteSkill(Long id) { skillMapper.deleteById(id); }

    @Override
    public Skill auditSkill(Long id, String status) {
        Skill skill = skillMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("技能不存在"));
        skill.setStatus(status);
        return skillMapper.save(skill);
    }

    @Override
    public List<Skill> getApprovedSkillsByUserId(Long userId) {
        return skillMapper.findByUserId(userId).stream()
                .filter(s -> "APPROVED".equals(s.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }
}