package com.airs.controller;

import com.airs.common.Result;
import com.airs.dto.RehabilitationPlanRequest;
import com.airs.entity.RehabilitationPlan;
import com.airs.service.RehabilitationPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rehabilitation-plans")
@RequiredArgsConstructor
public class RehabilitationPlanController {

    private final RehabilitationPlanService planService;

    @GetMapping("/injury/{injuryRecordId}")
    public Result<List<RehabilitationPlan>> getPlansByInjuryRecordId(@PathVariable Long injuryRecordId) {
        List<RehabilitationPlan> plans = planService.getPlansByInjuryRecordId(injuryRecordId);
        return Result.success(plans);
    }

    @GetMapping("/injury/{injuryRecordId}/active")
    public Result<RehabilitationPlan> getActivePlanByInjuryRecordId(@PathVariable Long injuryRecordId) {
        RehabilitationPlan plan = planService.getActivePlanByInjuryRecordId(injuryRecordId);
        return Result.success(plan);
    }

    @GetMapping("/{id}")
    public Result<RehabilitationPlan> getPlanById(@PathVariable Long id) {
        RehabilitationPlan plan = planService.getPlanById(id);
        return Result.success(plan);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'THERAPIST')")
    public Result<RehabilitationPlan> createPlan(@Valid @RequestBody RehabilitationPlanRequest request) {
        RehabilitationPlan plan = planService.createPlan(request);
        return Result.success(plan);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'THERAPIST')")
    public Result<RehabilitationPlan> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody RehabilitationPlanRequest request) {
        RehabilitationPlan plan = planService.updatePlan(id, request);
        return Result.success(plan);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'THERAPIST')")
    public Result<Void> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return Result.success();
    }
}
