package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "skill_exchanges")
public class SkillExchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User userA; // Requestor

    @ManyToOne
    private User userB; // Provider

    private String status; // REQUESTED, ACCEPTED, CONFIRMED, COMPLETED, NO_SHOW, CANCELLED
    private String description;
    private Integer zaowuBiCost;

    // 预约的日期
    private LocalDate scheduleDate;

    private Boolean userAConfirmed = false;
    private Boolean userBConfirmed = false;
    private Boolean userAReviewed = false;
    private Boolean userBReviewed = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
