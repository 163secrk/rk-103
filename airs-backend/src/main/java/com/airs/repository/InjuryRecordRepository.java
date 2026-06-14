package com.airs.repository;

import com.airs.entity.InjuryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InjuryRecordRepository extends JpaRepository<InjuryRecord, Long> {

    @Query("SELECT i FROM InjuryRecord i WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "i.athleteName LIKE %:keyword% OR " +
           "i.bodyPart LIKE %:keyword% OR " +
           "i.injuryType LIKE %:keyword% OR " +
           "i.severity LIKE %:keyword%)")
    Page<InjuryRecord> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<InjuryRecord> findByAthleteIdAndStatus(Long athleteId, String status);
}
