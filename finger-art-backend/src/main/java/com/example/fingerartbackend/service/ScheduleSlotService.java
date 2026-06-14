package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.ScheduleSlot;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleSlotService {
    List<ScheduleSlot> getSlotsByUserIdAndMonth(Long userId, int year, int month);
    ScheduleSlot saveOrUpdateSlot(Long userId, LocalDate date, String status, String remark);
}
