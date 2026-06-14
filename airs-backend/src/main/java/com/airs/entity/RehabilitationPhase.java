package com.airs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rehabilitation_phase")
public class RehabilitationPhase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long planId;

    @Column(nullable = false)
    private Integer phaseOrder;

    @Column(nullable = false, length = 50)
    private String phaseType;

    @Column(length = 50)
    private String phaseName;

    private Integer rpeUpperLimit;

    private Integer estimatedDays;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String goals;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
