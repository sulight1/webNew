package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 技能交换实体 {@link Skill} 的数据访问层。
 * <p>
 * 负责技能发布信息的持久化，支持按分类、用户、审核状态筛选。
 * </p>
 */
@Repository
public interface SkillMapper extends JpaRepository<Skill, Long> {

    /** 按分类查询技能列表 */
    List<Skill> findByCategory(String category);

    /** 按发布者用户 ID 查询其技能 */
    List<Skill> findByUserId(Long userId);

    /** 按审核/发布状态查询技能 */
    List<Skill> findByStatus(String status);

    /** 按分类与状态组合查询技能 */
    List<Skill> findByCategoryAndStatus(String category, String status);
}
