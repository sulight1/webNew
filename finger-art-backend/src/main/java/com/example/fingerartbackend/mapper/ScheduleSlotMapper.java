package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleSlotMapper extends JpaRepository<ScheduleSlot, Long> {
    List<ScheduleSlot> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    List<ScheduleSlot> findByUserIdAndDate(Long userId, LocalDate date);
    List<ScheduleSlot> findByUserId(Long userId);
}
