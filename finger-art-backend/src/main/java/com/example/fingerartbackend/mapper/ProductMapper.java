package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductMapper extends JpaRepository<Product, Long> {
    List<Product> findByCreatorId(Long creatorId);
    List<Product> findByCreator(String creator);
}