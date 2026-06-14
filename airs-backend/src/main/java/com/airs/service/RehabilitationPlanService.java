package com.airs.service;

import com.airs.dto.RehabilitationPlanRequest;
import com.airs.entity.InjuryRecord;
import com.airs.entity.RehabilitationPhase;
import com.airs.entity.RehabilitationPlan;
import com.airs.repository.InjuryRecordRepository;
import com.airs.repository.RehabilitationPhaseRepository;
import com.airs.repository.RehabilitationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RehabilitationPlanService {

    private final RehabilitationPlanRepository planRepository;
    private final RehabilitationPhaseRepository phaseRepository;
    private final InjuryRecordRepository injuryRecordRepository;

    public List<RehabilitationPlan> getPlansByInjuryRecordId(Long injuryRecordId) {
        return planRepository.findByInjuryRecordIdOrderByCreateTimeDesc(injuryRecordId);
    }

    public RehabilitationPlan getPlanById(Long id) {
        RehabilitationPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("康复计划不存在"));
        plan.setPhases(phaseRepository.findByPlanIdOrderByPhaseOrderAsc(id));
        return plan;
    }

    public RehabilitationPlan getActivePlanByInjuryRecordId(Long injuryRecordId) {
        Optional<RehabilitationPlan> planOpt = planRepository.findByInjuryRecordIdAndStatus(injuryRecordId, "ACTIVE");
        if (planOpt.isPresent()) {
            RehabilitationPlan plan = planOpt.get();
            plan.setPhases(phaseRepository.findByPlanIdOrderByPhaseOrderAsc(plan.getId()));
            return plan;
        }
        return null;
    }

    @Transactional
    public RehabilitationPlan createPlan(RehabilitationPlanRequest request) {
        InjuryRecord injuryRecord = injuryRecordRepository.findById(request.getInjuryRecordId())
                .orElseThrow(() -> new RuntimeException("伤病记录不存在"));

        RehabilitationPlan plan = new RehabilitationPlan();
        plan.setInjuryRecordId(request.getInjuryRecordId());
        plan.setPlanName(request.getPlanName());
        plan.setTherapistId(request.getTherapistId());
        plan.setTherapistName(request.getTherapistName());
        plan.setDescription(request.getDescription());
        plan.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        RehabilitationPlan savedPlan = planRepository.save(plan);

        List<RehabilitationPhase> phases = buildPhasesFromRequest(request.getPhases(), savedPlan.getId());
        List<RehabilitationPhase> savedPhases = phaseRepository.saveAll(phases);
        savedPlan.setPhases(savedPhases);

        return savedPlan;
    }

    @Transactional
    public RehabilitationPlan updatePlan(Long id, RehabilitationPlanRequest request) {
        RehabilitationPlan existingPlan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("康复计划不存在"));

        if (request.getInjuryRecordId() != null) {
            injuryRecordRepository.findById(request.getInjuryRecordId())
                    .orElseThrow(() -> new RuntimeException("伤病记录不存在"));
            existingPlan.setInjuryRecordId(request.getInjuryRecordId());
        }
        if (request.getPlanName() != null) {
            existingPlan.setPlanName(request.getPlanName());
        }
        if (request.getTherapistId() != null) {
            existingPlan.setTherapistId(request.getTherapistId());
        }
        if (request.getTherapistName() != null) {
            existingPlan.setTherapistName(request.getTherapistName());
        }
        if (request.getDescription() != null) {
            existingPlan.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            existingPlan.setStatus(request.getStatus());
        }

        planRepository.save(existingPlan);

        if (request.getPhases() != null) {
            phaseRepository.deleteByPlanId(id);
            List<RehabilitationPhase> phases = buildPhasesFromRequest(request.getPhases(), id);
            List<RehabilitationPhase> savedPhases = phaseRepository.saveAll(phases);
            existingPlan.setPhases(savedPhases);
        } else {
            existingPlan.setPhases(phaseRepository.findByPlanIdOrderByPhaseOrderAsc(id));
        }

        return existingPlan;
    }

    @Transactional
    public void deletePlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new RuntimeException("康复计划不存在");
        }
        phaseRepository.deleteByPlanId(id);
        planRepository.deleteById(id);
    }

    private List<RehabilitationPhase> buildPhasesFromRequest(
            List<RehabilitationPlanRequest.PhaseRequest> phaseRequests, Long planId) {
        List<RehabilitationPhase> phases = new ArrayList<>();
        for (RehabilitationPlanRequest.PhaseRequest pr : phaseRequests) {
            RehabilitationPhase phase = new RehabilitationPhase();
            phase.setPlanId(planId);
            phase.setPhaseOrder(pr.getPhaseOrder());
            phase.setPhaseType(pr.getPhaseType());
            phase.setPhaseName(pr.getPhaseName());
            phase.setRpeUpperLimit(pr.getRpeUpperLimit());
            phase.setEstimatedDays(pr.getEstimatedDays());
            phase.setDescription(pr.getDescription());
            phase.setGoals(pr.getGoals());
            phases.add(phase);
        }
        return phases;
    }
}
