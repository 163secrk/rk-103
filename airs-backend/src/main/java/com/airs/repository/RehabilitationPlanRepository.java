package com.airs.repository;

import com.airs.entity.RehabilitationPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RehabilitationPlanRepository extends JpaRepository<RehabilitationPlan, Long> {

    List<RehabilitationPlan> findByInjuryRecordIdOrderByCreateTimeDesc(Long injuryRecordId);

    Optional<RehabilitationPlan> findByInjuryRecordIdAndStatus(Long injuryRecordId, String status);
}
