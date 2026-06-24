package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 用户实体 {@link User} 的数据访问层。
 * <p>
 * 提供用户账号查询、角色统计等基础 CRUD 与自定义查询能力。
 * </p>
 */
@Repository
public interface UserMapper extends JpaRepository<User, Long> {

    /** 按用户名查找用户 */
    Optional<User> findByUsername(String username);

    /** 按数字账号查找用户 */
    Optional<User> findByAccount(String account);

    /** 判断数字账号是否已存在 */
    boolean existsByAccount(String account);

    /** 统计指定角色的用户数量 */
    long countByRole(String role);

    /** 查询指定角色的全部用户 */
    List<User> findByRole(String role);
}
