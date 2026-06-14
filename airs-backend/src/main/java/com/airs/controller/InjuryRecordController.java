package com.airs.controller;

import com.airs.common.PageResult;
import com.airs.common.Result;
import com.airs.entity.InjuryRecord;
import com.airs.service.InjuryRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/injury-records")
@RequiredArgsConstructor
public class InjuryRecordController {
    private final InjuryRecordService injuryRecordService;

    @GetMapping
    public Result<PageResult<InjuryRecord>> getInjuryRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        PageResult<InjuryRecord> page = injuryRecordService.getInjuryRecordPage(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<InjuryRecord> getInjuryRecordById(@PathVariable Long id) {
        InjuryRecord record = injuryRecordService.getInjuryRecordById(id);
        return Result.success(record);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<InjuryRecord> createInjuryRecord(@RequestBody InjuryRecord injuryRecord) {
        InjuryRecord created = injuryRecordService.createInjuryRecord(injuryRecord);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<InjuryRecord> updateInjuryRecord(@PathVariable Long id, @RequestBody InjuryRecord injuryRecord) {
        InjuryRecord updated = injuryRecordService.updateInjuryRecord(id, injuryRecord);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public Result<Void> deleteInjuryRecord(@PathVariable Long id) {
        injuryRecordService.deleteInjuryRecord(id);
        return Result.success();
    }
}
