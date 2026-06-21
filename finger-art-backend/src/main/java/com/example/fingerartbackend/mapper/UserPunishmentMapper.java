package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.UserPunishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserPunishmentMapper extends JpaRepository<UserPunishment, Long> {

    @Query("SELECT p FROM UserPunishment p WHERE p.userId = :userId AND (p.endAt IS NULL OR p.endAt > :now) ORDER BY p.createdAt DESC")
    List<UserPunishment> findActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    List<UserPunishment> findByUserIdAndType(Long userId, String type);

    void deleteByUserIdAndType(Long userId, String type);
}
