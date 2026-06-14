package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CustomOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomOrderMapper extends JpaRepository<CustomOrder, Long> {
    List<CustomOrder> findByArtisanId(Long artisanId);
    List<CustomOrder> findByBuyerId(Long buyerId);
    List<CustomOrder> findByStatusOrderByCreateTimeDesc(String status);
    long countByStatus(String status);
}
