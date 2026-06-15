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

import java.util.List;

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

        InjuryRecord saved = injuryRecordRepository.save(injuryRecord);
        updateAthleteStatusAfterInjuryChange(athlete);
        return saved;
    }

    @Transactional
    public InjuryRecord updateInjuryRecord(Long id, InjuryRecord injuryRecord) {
        InjuryRecord existing = getInjuryRecordById(id);
        Long originalAthleteId = existing.getAthleteId();
        String originalStatus = existing.getStatus();

        existing.setBodyPart(injuryRecord.getBodyPart());
        existing.setInjuryType(injuryRecord.getInjuryType());
        existing.setSeverity(injuryRecord.getSeverity());
        existing.setDescription(injuryRecord.getDescription());
        existing.setInjuryDate(injuryRecord.getInjuryDate());
        existing.setStatus(injuryRecord.getStatus());
        existing.setTreatment(injuryRecord.getTreatment());
        existing.setDoctorId(injuryRecord.getDoctorId());
        existing.setDoctorName(injuryRecord.getDoctorName());

        boolean athleteChanged = injuryRecord.getAthleteId() != null && !injuryRecord.getAthleteId().equals(existing.getAthleteId());
        boolean statusChanged = injuryRecord.getStatus() != null && !injuryRecord.getStatus().equals(originalStatus);

        if (athleteChanged) {
            Athlete oldAthlete = athleteRepository.findById(originalAthleteId).orElse(null);
            if (oldAthlete != null) {
                updateAthleteStatusAfterInjuryChange(oldAthlete);
            }
            existing.setAthleteId(injuryRecord.getAthleteId());
            Athlete newAthlete = athleteRepository.findById(injuryRecord.getAthleteId()).orElse(null);
            if (newAthlete != null) {
                existing.setAthleteName(newAthlete.getName());
            }
        }

        InjuryRecord saved = injuryRecordRepository.save(existing);

        if (athleteChanged) {
            Athlete newAthlete = athleteRepository.findById(injuryRecord.getAthleteId()).orElse(null);
            if (newAthlete != null) {
                updateAthleteStatusAfterInjuryChange(newAthlete);
            }
        } else if (statusChanged || (injuryRecord.getSeverity() != null && !injuryRecord.getSeverity().equals(existing.getSeverity()))) {
            Athlete athlete = athleteRepository.findById(originalAthleteId).orElse(null);
            if (athlete != null) {
                updateAthleteStatusAfterInjuryChange(athlete);
            }
        }

        return saved;
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

    private void updateAthleteStatusAfterInjuryChange(Athlete athlete) {
        List<InjuryRecord> activeRecords = injuryRecordRepository.findByAthleteIdAndStatus(athlete.getId(), "ACTIVE");
        List<InjuryRecord> rehabRecords = injuryRecordRepository.findByAthleteIdAndStatus(athlete.getId(), "REHAB");
        if (!activeRecords.isEmpty()) {
            athlete.setStatus("INJURED");
        } else if (!rehabRecords.isEmpty()) {
            athlete.setStatus("REHAB");
        } else {
            athlete.setStatus("HEALTHY");
        }
        athleteRepository.save(athlete);
    }
}
