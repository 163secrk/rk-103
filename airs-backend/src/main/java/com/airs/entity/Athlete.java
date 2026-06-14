package com.airs.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "athlete")
public class Athlete {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, length = 20)
    private String jerseyNumber;

    @Column(length = 30)
    private String position;

    @Column(length = 10)
    private String gender;

    private LocalDate birthDate;

    @Column(length = 10)
    private String height;

    @Column(length = 10)
    private String weight;

    @Column(length = 255)
    private String avatar;

    @Column(length = 20)
    private String status = "HEALTHY";

    @Column(length = 500)
    private String remark;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
