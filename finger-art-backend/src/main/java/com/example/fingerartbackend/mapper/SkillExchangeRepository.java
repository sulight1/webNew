package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.SkillExchange;
import com.example.fingerartbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SkillExchangeRepository extends JpaRepository<SkillExchange, Long> {
    @Query("SELECT s FROM SkillExchange s WHERE s.userA = :user OR s.userB = :user")
    List<SkillExchange> findByUser(User user);

    List<SkillExchange> findByStatusInAndScheduleDateBefore(java.util.List<String> statuses, java.time.LocalDate date);
}
