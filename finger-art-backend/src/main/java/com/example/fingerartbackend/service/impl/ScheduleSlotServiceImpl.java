package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.ScheduleSlot;
import com.example.fingerartbackend.mapper.ScheduleSlotMapper;
import com.example.fingerartbackend.service.ScheduleSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleSlotServiceImpl implements ScheduleSlotService {

    @Autowired
    private ScheduleSlotMapper slotMapper;

    @Override
    public List<ScheduleSlot> getSlotsByUserIdAndMonth(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return slotMapper.findByUserIdAndDateBetween(userId, start, end);
    }

    @Override
    public ScheduleSlot saveOrUpdateSlot(Long userId, LocalDate date, String status, String remark) {
        List<ScheduleSlot> existing = slotMapper.findByUserIdAndDate(userId, date);
        ScheduleSlot slot;
        if (!existing.isEmpty()) {
            slot = existing.get(0);
        } else {
            slot = new ScheduleSlot();
            slot.setUserId(userId);
            slot.setDate(date);
        }
        slot.setStatus(status);
        if (remark != null) slot.setRemark(remark);
        return slotMapper.save(slot);
    }
}
