package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.entity.ScheduleSlot;
import com.example.fingerartbackend.service.ScheduleSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 档期排期控制器。
 * 管理手作达人的可接单档期与忙碌状态，对应技能交换与接单排期模块。
 */
@RestController
@RequestMapping("/schedule-slots")
public class ScheduleSlotController {

    @Autowired
    private ScheduleSlotService slotService;

    /**
     * 查询指定用户某年某月的档期列表。
     *
     * @param userId 用户 ID
     * @param year   年份
     * @param month  月份（1-12）
     * @return 该月档期记录列表
     */
    @GetMapping
    public Result<List<ScheduleSlot>> getSlots(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return Result.success(slotService.getSlotsByUserIdAndMonth(userId, year, month));
    }

    /**
     * 新增或更新某日的档期状态。
     *
     * @param userId 用户 ID
     * @param date   日期，格式 yyyy-MM-dd
     * @param status 档期状态（如可接单、忙碌等）
     * @param remark 可选备注
     * @return 保存后的档期记录
     */
    @PostMapping
    public Result<ScheduleSlot> saveSlot(
            @RequestParam Long userId,
            @RequestParam String date,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        return Result.success(slotService.saveOrUpdateSlot(userId, LocalDate.parse(date), status, remark));
    }
}
