package com.interview.management.repository;

import org.springframework.stereotype.Repository;
import com.interview.management.entity.InterviewStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface InterviewStatusHistoryRepository extends JpaRepository<InterviewStatusHistory, Long> {
    
    /**
     * Find all status changes for an interview
     * Audit trail
     */
    List<InterviewStatusHistory> findByInterviewIdOrderByChangedAtAsc(Long interviewId);
    
    /**
     * Find who cancelled interviews
     * Useful for analytics/accountability
     */
    @Query("SELECT h.changedBy, COUNT(h) FROM InterviewStatusHistory h " +
           "WHERE h.status = 'CANCELLED' " +
           "GROUP BY h.changedBy")
    List<Object[]> findCancellationsByUser();
    
    /**
     * Find recent status changes (last 7 days)
     * For activity feed
     */
    @Query("SELECT h FROM InterviewStatusHistory h " +
           "WHERE h.changedAt >= :since " +
           "ORDER BY h.changedAt DESC")
    List<InterviewStatusHistory> findRecentChanges(@Param("since") LocalDateTime since);
}

//