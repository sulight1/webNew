package com.example.fingerartbackend.config;

import com.example.fingerartbackend.entity.Skill;
import com.example.fingerartbackend.mapper.SkillMapper;
import com.example.fingerartbackend.util.SkillCategoryInferrer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 技能分类数据迁移 Runner。
 * <p>
 * 应用启动时扫描已发布技能，根据标题/描述推断正确分类并修正错误数据，
 * 不触发重新审核流程。
 * </p>
 */
@Component
public class SkillCategoryMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SkillCategoryMigrationRunner.class);

    private final SkillMapper skillMapper;

    public SkillCategoryMigrationRunner(SkillMapper skillMapper) {
        this.skillMapper = skillMapper;
    }

    /**
     * 执行 run 相关逻辑。
     */
    @Override
    public void run(ApplicationArguments args) {
        List<Skill> skills = skillMapper.findAll();
        int fixed = 0;
        for (Skill skill : skills) {
            if (!SkillCategoryInferrer.shouldFix(skill.getCategory(), skill.getTitle(), skill.getDescription())) {
                continue;
            }
            String inferred = SkillCategoryInferrer.infer(
                    skill.getCategory(), skill.getTitle(), skill.getDescription());
            String before = skill.getCategory();
            skill.setCategory(inferred);
            skillMapper.save(skill);
            fixed++;
            log.info("技能分类已修正 id={} title={} : {} -> {}", skill.getId(), skill.getTitle(), before, inferred);
        }
        if (fixed > 0) {
            log.info("共修正 {} 条技能分类", fixed);
        }
    }
}
