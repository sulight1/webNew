package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SkillMapper extends JpaRepository<Skill, Long> {
    List<Skill> findByCategory(String category);
    List<Skill> findByUserId(Long userId);
    List<Skill> findByStatus(String status);
    List<Skill> findByCategoryAndStatus(String category, String status);
}