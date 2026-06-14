package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensitiveWordMapper extends JpaRepository<SensitiveWord, Long> {
    List<SensitiveWord> findByEnabledTrue();
    boolean existsByWord(String word);
}
