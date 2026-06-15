package com.airs.repository;

import com.airs.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "u.username LIKE %:keyword% OR " +
           "u.realName LIKE %:keyword% OR " +
           "u.email LIKE %:keyword%) AND " +
           "(:role IS NULL OR :role = '' OR u.role = :role)")
    Page<User> findByConditions(@Param("keyword") String keyword,
                                @Param("role") String role,
                                Pageable pageable);
}
