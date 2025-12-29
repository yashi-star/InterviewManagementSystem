package com.interview.management.repository;


import com.interview.management.entity.Interviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ============================================
// INTERVIEWER REPOSITORY
// ============================================

@Repository
public interface InterviewerRepository extends JpaRepository<Interviewer, Long> {
    
    /**
     * Find interviewer by email
     */
    Optional<Interviewer> findByEmail(String email);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all interviewers in a department
     */
    List<Interviewer> findByDepartment(String department);
    
    /**
     * Find interviewers by expertise area
     * Example: Find all interviewers who know "Java"
     */
    @Query("SELECT i FROM Interviewer i WHERE LOWER(i.expertise) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Interviewer> findByExpertiseContaining(@Param("skill") String skill);
    
    /**
     * Find available interviewers (no conflicts in time slot)
     * Complex query that excludes busy interviewers
     */
    @Query("SELECT i FROM Interviewer i WHERE i.id NOT IN (" +
           "SELECT DISTINCT int.interviewer.id FROM Interview int " +
           "WHERE int.scheduledAt BETWEEN :startTime AND :endTime " +
           "AND int.currentStatus = 'SCHEDULED')")
    List<Interviewer> findAvailableInterviewers(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
