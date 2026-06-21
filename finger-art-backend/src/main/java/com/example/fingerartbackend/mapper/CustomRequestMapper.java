package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CustomRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomRequestMapper extends JpaRepository<CustomRequest, Long>, JpaSpecificationExecutor<CustomRequest> {
    List<CustomRequest> findByBuyerId(Long buyerId);
    long countByStatus(String status);
}
