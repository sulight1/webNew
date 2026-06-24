package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.SkillExchange;
import com.example.fingerartbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 技能交换预约实体 {@link SkillExchange} 的数据访问层。
 * <p>
 * 记录双方用户的技能互换预约，支持按参与用户及过期状态查询。
 * </p>
 */
@Repository
public interface SkillExchangeRepository extends JpaRepository<SkillExchange, Long> {

    /** 查询用户作为任一方参与的全部技能交换记录 */
    @Query("SELECT s FROM SkillExchange s WHERE s.userA = :user OR s.userB = :user")
    List<SkillExchange> findByUser(User user);

    /** 查询指定状态且预约日期已过的交换记录，用于定时过期处理 */
    List<SkillExchange> findByStatusInAndScheduleDateBefore(java.util.List<String> statuses, java.time.LocalDate date);
}
