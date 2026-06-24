package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 敏感词实体 {@link SensitiveWord} 的数据访问层。
 * <p>
 * 负责内容审核敏感词库的持久化及启用状态查询。
 * </p>
 */
@Repository
public interface SensitiveWordMapper extends JpaRepository<SensitiveWord, Long> {

    /** 查询全部已启用的敏感词 */
    List<SensitiveWord> findByEnabledTrue();

    /** 判断敏感词是否已存在 */
    boolean existsByWord(String word);
}
