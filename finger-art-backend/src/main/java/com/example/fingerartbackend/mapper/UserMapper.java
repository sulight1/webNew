package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMapper extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByAccount(String account);

    boolean existsByAccount(String account);

    long countByRole(String role);
    List<User> findByRole(String role);
}