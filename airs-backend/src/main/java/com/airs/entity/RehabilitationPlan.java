package com.airs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "rehabilitation_plan")
public class RehabilitationPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long injuryRecordId;

    @Column(length = 200)
    private String planName;

    private Long therapistId;

    @Column(length = 50)
    private String therapistName;

    @Column(length = 500)
    private String description;

    @Column(length = 20)
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "planId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("phaseOrder ASC")
    private List<RehabilitationPhase> phases = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
