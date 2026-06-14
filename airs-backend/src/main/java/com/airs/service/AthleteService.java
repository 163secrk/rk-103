package com.airs.service;

import com.airs.common.PageResult;
import com.airs.entity.Athlete;
import com.airs.repository.AthleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AthleteService {
    private final AthleteRepository athleteRepository;

    public PageResult<Athlete> getAthletePage(int pageNum, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Athlete> page = athleteRepository.findByKeyword(keyword, pageable);
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                pageNum,
                pageSize
        );
    }

    public Athlete getAthleteById(Long id) {
        return athleteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("运动员不存在"));
    }

    @Transactional
    public Athlete createAthlete(Athlete athlete) {
        return athleteRepository.save(athlete);
    }

    @Transactional
    public Athlete updateAthlete(Long id, Athlete athlete) {
        Athlete existing = getAthleteById(id);
        existing.setName(athlete.getName());
        existing.setJerseyNumber(athlete.getJerseyNumber());
        existing.setPosition(athlete.getPosition());
        existing.setGender(athlete.getGender());
        existing.setBirthDate(athlete.getBirthDate());
        existing.setHeight(athlete.getHeight());
        existing.setWeight(athlete.getWeight());
        existing.setAvatar(athlete.getAvatar());
        existing.setStatus(athlete.getStatus());
        existing.setRemark(athlete.getRemark());
        return athleteRepository.save(existing);
    }

    @Transactional
    public void deleteAthlete(Long id) {
        if (!athleteRepository.existsById(id)) {
            throw new RuntimeException("运动员不存在");
        }
        athleteRepository.deleteById(id);
    }
}
