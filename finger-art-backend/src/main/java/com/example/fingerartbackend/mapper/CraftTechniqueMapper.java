package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CraftTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 手工艺技法实体 {@link CraftTechnique} 的数据访问层。
 * <p>
 * 负责平台工艺百科/技法库数据的持久化与按分类查询。
 * </p>
 */
@Repository
public interface CraftTechniqueMapper extends JpaRepository<CraftTechnique, Long> {

    /** 按工艺分类查询技法列表 */
    List<CraftTechnique> findByCategory(String category);

    /** 查询全部技法，按分类升序排列 */
    List<CraftTechnique> findAllByOrderByCategoryAsc();
}
