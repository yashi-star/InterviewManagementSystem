package com.interview.management.repository;


import com.interview.management.entity.Interview;
import com.interview.management.entity.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interview Repository
 * 
 * Key Feature: Conflict Detection Queries
 * Used to prevent double-booking interviewers
 */
@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    
    // ============================================
    // CONFLICT DETECTION QUERIES
    // ============================================
    
    /**
     * CRITICAL: Find interviews for interviewer in a time range
     * Used for conflict detection before scheduling
     * 
     * Strategy: Query interviews that might overlap with proposed time
     * Then use Interview.overlapsWith() method to check exact overlap
     * 
     * Why ±2 hours? Accounts for interview duration buffer
     */
    @Query("SELECT i FROM Interview i WHERE " +
           "i.interviewer.id = :interviewerId AND " +
           "i.currentStatus NOT IN ('CANCELLED', 'COMPLETED') AND " +
           "i.scheduledAt BETWEEN :startTime AND :endTime")
    List<Interview> findByInterviewerIdAndScheduledAtBetween(
        @Param("interviewerId") Long interviewerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Alternative: Direct overlap check in database
     * More complex but eliminates need for Java-side checking
     * 
     * Overlap logic: (thisStart < otherEnd) AND (thisEnd > otherStart)
     */
    @Query("SELECT i FROM Interview i WHERE " +
           "i.interviewer.id = :interviewerId AND " +
           "i.currentStatus = 'SCHEDULED' AND " +
           "i.scheduledAt < :proposedEnd AND " +
           "FUNCTION('TIMESTAMPADD', MINUTE, i.durationMinutes, i.scheduledAt) > :proposedStart")
    List<Interview> findConflictingInterviews(
        @Param("interviewerId") Long interviewerId,
        @Param("proposedStart") LocalDateTime proposedStart,
        @Param("proposedEnd") LocalDateTime proposedEnd
    );
    
    // ============================================
    // DASHBOARD QUERIES
    // ============================================
    
    /**
     * DASHBOARD: Count interviews scheduled today
     * Uses DATE() function to compare only date part
     */
    @Query("SELECT COUNT(i) FROM Interview i WHERE " +
           "CAST(i.scheduledAt AS date) = CAST(:today AS date) AND " +
           "i.currentStatus = 'SCHEDULED'")
    Long countInterviewsScheduledToday(@Param("today") LocalDateTime today);
    
    /**
     * Native SQL version (more efficient)
     */
    @Query(value = 
        "SELECT COUNT(*) FROM interviews " +
        "WHERE DATE(scheduled_at) = DATE(:today) " +
        "AND current_status = 'SCHEDULED'",
        nativeQuery = true
    )
    Long countInterviewsScheduledTodayNative(@Param("today") LocalDateTime today);
    
    /**
     * Find today's interviews with full details
     * Used for "Today's Schedule" dashboard widget
     */
    @Query("SELECT i FROM Interview i " +
           "LEFT JOIN FETCH i.candidate " +
           "LEFT JOIN FETCH i.interviewer " +
           "WHERE CAST(i.scheduledAt AS date) = CAST(:today AS date) " +
           "AND i.currentStatus IN ('SCHEDULED', 'IN_PROGRESS') " +
           "ORDER BY i.scheduledAt ASC")
    List<Interview> findTodaysInterviews(@Param("today") LocalDateTime today);
    
    // ============================================
    // CANDIDATE & INTERVIEWER QUERIES
    // ============================================
    
    /**
     * Find all interviews for a candidate
     */
    List<Interview> findByCandidateIdOrderByScheduledAtDesc(Long candidateId);
    
    /**
     * Find all interviews conducted by an interviewer
     */
    List<Interview> findByInterviewerIdOrderByScheduledAtDesc(Long interviewerId);
    
    /**
     * Find upcoming interviews for a candidate
     */
    @Query("SELECT i FROM Interview i WHERE " +
           "i.candidate.id = :candidateId AND " +
           "i.scheduledAt > :now AND " +
           "i.currentStatus = 'SCHEDULED'")
    List<Interview> findUpcomingInterviewsForCandidate(
        @Param("candidateId") Long candidateId,
        @Param("now") LocalDateTime now
    );
    
    // ============================================
    // STATUS QUERIES
    // ============================================
    
    /**
     * Find interviews by status
     */
    List<Interview> findByCurrentStatus(InterviewStatus status);
    
    /**
     * Find interviews that need feedback
     * Completed interviews without feedback entries
     */
    @Query("SELECT i FROM Interview i WHERE " +
           "i.currentStatus = 'COMPLETED' AND " +
           "NOT EXISTS (SELECT 1 FROM Feedback f WHERE f.interview = i)")
    List<Interview> findCompletedInterviewsWithoutFeedback();
    
    /**
     * Count pending feedbacks (for dashboard)
     */
    @Query("SELECT COUNT(i) FROM Interview i WHERE " +
           "i.currentStatus = 'COMPLETED' AND " +
           "NOT EXISTS (SELECT 1 FROM Feedback f WHERE f.interview = i)")
    Long countPendingFeedbacks();
    
    // ============================================
    // DATE RANGE QUERIES
    // ============================================
    
    /**
     * Find interviews in a date range
     * Useful for calendar view
     */
    @Query("SELECT i FROM Interview i WHERE " +
           "i.scheduledAt BETWEEN :startDate AND :endDate " +
           "ORDER BY i.scheduledAt ASC")
    List<Interview> findInterviewsInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find interviews scheduled this week
     */
    @Query(value = 
        "SELECT * FROM interviews " +
        "WHERE scheduled_at >= DATE_TRUNC('week', CURRENT_DATE) " +
        "AND scheduled_at < DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '1 week' " +
        "ORDER BY scheduled_at",
        nativeQuery = true
    )
    List<Interview> findInterviewsThisWeek();
    
    // ============================================
    // ANALYTICS QUERIES
    // ============================================
    
    /**
     * Get interview statistics by type
     */
    @Query("SELECT i.interviewType, COUNT(i), " +
           "SUM(CASE WHEN i.currentStatus = 'COMPLETED' THEN 1 ELSE 0 END) " +
           "FROM Interview i " +
           "GROUP BY i.interviewType")
    List<Object[]> getInterviewStatsByType();
    
    /**
     * Find interviews that are overdue (scheduled in past, still in SCHEDULED status)
     * These might indicate data inconsistencies
     */
    @Query("SELECT i FROM Interview i WHERE " +
           "i.scheduledAt < :now AND " +
           "i.currentStatus = 'SCHEDULED'")
    List<Interview> findOverdueInterviews(@Param("now") LocalDateTime now);
    
    /**
     * Average interview duration by type
     */
    @Query("SELECT i.interviewType, AVG(i.durationMinutes) " +
           "FROM Interview i " +
           "WHERE i.currentStatus = 'COMPLETED' " +
           "GROUP BY i.interviewType")
    List<Object[]> getAverageDurationByType();
}

/**
 * INTERVIEW QUESTIONS YOU MIGHT BE ASKED:
 * 
 * Q1: How do you prevent double-booking?
 * A: Query for existing interviews in the time range, then use 
 *    Interview.overlapsWith() to check for conflicts. Throw 
 *    SchedulingConflictException if overlap detected.
 * 
 * Q2: Why use JPQL FUNCTION('TIMESTAMPADD', ...)?
 * A: To add minutes to timestamp in database-independent way.
 *    Alternative: Use native SQL with PostgreSQL-specific syntax.
 * 
 * Q3: What's the N+1 problem in findTodaysInterviews()?
 * A: Without JOIN FETCH, loading interviews would trigger separate
 *    queries for each candidate and interviewer. JOIN FETCH loads
 *    everything in one query.
 * 
 * Q4: Why both JPQL and Native SQL versions?
 * A: JPQL is database-independent (portable). Native SQL is more
 *    powerful for complex queries and PostgreSQL-specific functions.
 */
