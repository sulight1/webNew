package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * 匠人档期实体 {@link ScheduleSlot} 的数据访问层。
 * <p>
 * 负责技能交换/定制接单可预约时间段的持久化与查询。
 * </p>
 */
@Repository
public interface ScheduleSlotMapper extends JpaRepository<ScheduleSlot, Long> {

    /** 查询某用户在日期区间内的档期 */
    List<ScheduleSlot> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    /** 查询某用户某日的档期 */
    List<ScheduleSlot> findByUserIdAndDate(Long userId, LocalDate date);

    /** 查询某用户的全部档期 */
    List<ScheduleSlot> findByUserId(Long userId);
}
