package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CraftTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CraftTechniqueMapper extends JpaRepository<CraftTechnique, Long> {
    List<CraftTechnique> findByCategory(String category);
    List<CraftTechnique> findAllByOrderByCategoryAsc();
}
