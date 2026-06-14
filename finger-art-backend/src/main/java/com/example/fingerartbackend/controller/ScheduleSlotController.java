package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.ScheduleSlot;
import com.example.fingerartbackend.service.ScheduleSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/schedule-slots")
public class ScheduleSlotController {

    @Autowired
    private ScheduleSlotService slotService;

    @GetMapping
    public Result<List<ScheduleSlot>> getSlots(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return Result.success(slotService.getSlotsByUserIdAndMonth(userId, year, month));
    }

    @PostMapping
    public Result<ScheduleSlot> saveSlot(
            @RequestParam Long userId,
            @RequestParam String date,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        return Result.success(slotService.saveOrUpdateSlot(userId, LocalDate.parse(date), status, remark));
    }
}
