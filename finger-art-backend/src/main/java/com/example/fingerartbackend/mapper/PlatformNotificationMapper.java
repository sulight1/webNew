package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.PlatformNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformNotificationMapper extends JpaRepository<PlatformNotification, Long> {
    List<PlatformNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    void deleteByUserIdAndIsReadTrue(Long userId);
}
