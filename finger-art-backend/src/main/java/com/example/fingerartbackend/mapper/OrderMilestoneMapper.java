package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.OrderMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderMilestoneMapper extends JpaRepository<OrderMilestone, Long> {
    List<OrderMilestone> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
