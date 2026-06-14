package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewMapper extends JpaRepository<Review, Long> {
    List<Review> findByToUserIdOrderByCreatedAtDesc(Long toUserId);
    Optional<Review> findByOrderIdAndFromUserId(Long orderId, Long fromUserId);
    Optional<Review> findByExchangeIdAndFromUserId(Long exchangeId, Long fromUserId);
    boolean existsByOrderIdAndFromUserId(Long orderId, Long fromUserId);
    boolean existsByExchangeIdAndFromUserId(Long exchangeId, Long fromUserId);
}
