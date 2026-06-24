package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 作品/商品实体 {@link Product} 的数据访问层。
 * <p>
 * 负责市集作品的持久化及按创作者维度的查询。
 * </p>
 */
@Repository
public interface ProductMapper extends JpaRepository<Product, Long> {

    /** 按创作者用户 ID 查询其作品列表 */
    List<Product> findByCreatorId(Long creatorId);

    /** 按创作者名称查询其作品列表 */
    List<Product> findByCreator(String creator);
}
