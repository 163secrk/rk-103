package com.airs.service;

import com.airs.common.PageResult;
import com.airs.entity.Athlete;
import com.airs.entity.InjuryRecord;
import com.airs.repository.AthleteRepository;
import com.airs.repository.InjuryRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InjuryRecordService {
    private final InjuryRecordRepository injuryRecordRepository;
    private final AthleteRepository athleteRepository;

    public PageResult<InjuryRecord> getInjuryRecordPage(int pageNum, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<InjuryRecord> page = injuryRecordRepository.findByKeyword(keyword, pageable);
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                pageNum,
                pageSize
        );
    }

    public InjuryRecord getInjuryRecordById(Long id) {
        return injuryRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("伤病记录不存在"));
    }

    @Transactional
    public InjuryRecord createInjuryRecord(InjuryRecord injuryRecord) {
        Athlete athlete = athleteRepository.findById(injuryRecord.getAthleteId())
                .orElseThrow(() -> new RuntimeException("运动员不存在"));

        if (injuryRecord.getAthleteName() == null || injuryRecord.getAthleteName().isEmpty()) {
            injuryRecord.setAthleteName(athlete.getName());
        }

        String status = determineAthleteStatus(injuryRecord.getSeverity());
        athlete.setStatus(status);
        athleteRepository.save(athlete);

        return injuryRecordRepository.save(injuryRecord);
    }

    @Transactional
    public InjuryRecord updateInjuryRecord(Long id, InjuryRecord injuryRecord) {
        InjuryRecord existing = getInjuryRecordById(id);
        existing.setBodyPart(injuryRecord.getBodyPart());
        existing.setInjuryType(injuryRecord.getInjuryType());
        existing.setSeverity(injuryRecord.getSeverity());
        existing.setDescription(injuryRecord.getDescription());
        existing.setInjuryDate(injuryRecord.getInjuryDate());
        existing.setStatus(injuryRecord.getStatus());
        existing.setTreatment(injuryRecord.getTreatment());

        if (injuryRecord.getAthleteId() != null && !injuryRecord.getAthleteId().equals(existing.getAthleteId())) {
            Athlete oldAthlete = athleteRepository.findById(existing.getAthleteId()).orElse(null);
            if (oldAthlete != null) {
                updateAthleteStatusAfterInjuryChange(oldAthlete);
            }
            existing.setAthleteId(injuryRecord.getAthleteId());
            Athlete newAthlete = athleteRepository.findById(injuryRecord.getAthleteId()).orElse(null);
            if (newAthlete != null) {
                existing.setAthleteName(newAthlete.getName());
                String status = determineAthleteStatus(injuryRecord.getSeverity());
                newAthlete.setStatus(status);
                athleteRepository.save(newAthlete);
            }
        } else if (injuryRecord.getSeverity() != null && !injuryRecord.getSeverity().equals(existing.getSeverity())) {
            Athlete athlete = athleteRepository.findById(existing.getAthleteId()).orElse(null);
            if (athlete != null) {
                String status = determineAthleteStatus(injuryRecord.getSeverity());
                athlete.setStatus(status);
                athleteRepository.save(athlete);
            }
        }

        return injuryRecordRepository.save(existing);
    }

    @Transactional
    public void deleteInjuryRecord(Long id) {
        InjuryRecord record = injuryRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("伤病记录不存在"));

        injuryRecordRepository.deleteById(id);

        Athlete athlete = athleteRepository.findById(record.getAthleteId()).orElse(null);
        if (athlete != null) {
            updateAthleteStatusAfterInjuryChange(athlete);
        }
    }

    private String determineAthleteStatus(String severity) {
        if (severity == null) {
            return "INJURED";
        }
        switch (severity) {
            case "轻度":
            case "MILD":
                return "MILD_INJURY";
            case "中度":
            case "MODERATE":
                return "INJURED";
            case "重度":
            case "SEVERE":
                return "SERIOUS_INJURY";
            default:
                return "INJURED";
        }
    }

    private void updateAthleteStatusAfterInjuryChange(Athlete athlete) {
        long activeCount = injuryRecordRepository.findByAthleteIdAndStatus(athlete.getId(), "ACTIVE").size();
        if (activeCount == 0) {
            athlete.setStatus("HEALTHY");
        } else {
            athlete.setStatus("INJURED");
        }
        athleteRepository.save(athlete);
    }
}
