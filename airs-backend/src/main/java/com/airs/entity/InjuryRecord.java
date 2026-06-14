package com.airs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "injury_record")
public class InjuryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long athleteId;

    @Column(length = 100)
    private String athleteName;

    @Column(length = 50)
    private String bodyPart;

    @Column(length = 50)
    private String injuryType;

    @Column(length = 20)
    private String severity;

    @Column(length = 500)
    private String description;

    private LocalDate injuryDate;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(length = 500)
    private String treatment;

    private Long doctorId;

    @Column(length = 50)
    private String doctorName;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
