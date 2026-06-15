package com.airs.controller;

import com.airs.common.PageResult;
import com.airs.common.Result;
import com.airs.entity.Athlete;
import com.airs.service.AthleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/athletes")
@RequiredArgsConstructor
public class AthleteController {
    private final AthleteService athleteService;

    @GetMapping
    public Result<PageResult<Athlete>> getAthletes(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        PageResult<Athlete> page = athleteService.getAthletePage(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Athlete> getAthleteById(@PathVariable Long id) {
        Athlete athlete = athleteService.getAthleteById(id);
        return Result.success(athlete);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<Athlete> createAthlete(@RequestBody Athlete athlete) {
        Athlete created = athleteService.createAthlete(athlete);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<Athlete> updateAthlete(@PathVariable Long id, @RequestBody Athlete athlete) {
        Athlete updated = athleteService.updateAthlete(id, athlete);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<Void> deleteAthlete(@PathVariable Long id) {
        athleteService.deleteAthlete(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Athlete>> getAllAthletes() {
        return Result.success(athleteService.getAllAthletes());
    }
}
