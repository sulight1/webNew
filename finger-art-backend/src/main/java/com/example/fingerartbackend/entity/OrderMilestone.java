package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "order_milestones")
public class OrderMilestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    /** CONFIRM / DEPOSIT / PRODUCING / SHIP / ACCEPT / BALANCE / COMPLETE */
    private String stageKey;

    private String stageLabel;

    @Column(columnDefinition = "TEXT")
    private String note;

    private String imageUrl;

    private Long operatorId;
    private String operatorName;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
