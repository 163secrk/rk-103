package com.airs.repository;

import com.airs.entity.RehabilitationPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RehabilitationPhaseRepository extends JpaRepository<RehabilitationPhase, Long> {

    List<RehabilitationPhase> findByPlanIdOrderByPhaseOrderAsc(Long planId);

    void deleteByPlanId(Long planId);
}
