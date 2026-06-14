package com.example.fingerartbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "custom_request_bids", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "request_id", "artisan_id" })
})
public class CustomRequestBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "artisan_id", nullable = false)
    private Long artisanId;

    @Column(columnDefinition = "TEXT")
    private String message;

    /** PENDING / SELECTED / REJECTED */
    private String status = "PENDING";

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
