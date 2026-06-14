package com.airs.repository;

import com.airs.entity.Athlete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Long> {
    @Query("SELECT a FROM Athlete a WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "a.name LIKE %:keyword% OR " +
           "a.jerseyNumber LIKE %:keyword% OR " +
           "a.position LIKE %:keyword%)")
    Page<Athlete> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
