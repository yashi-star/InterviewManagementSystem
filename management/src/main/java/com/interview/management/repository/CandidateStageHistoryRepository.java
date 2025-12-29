package com.interview.management.repository;

import com.interview.management.entity.CandidateStageHistory;
import com.interview.management.entity.enums.CandidateStage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CandidateStageHistoryRepository extends JpaRepository<CandidateStageHistory, Long> {
    
    /**
     * Find all stage changes for a candidate
     * Shows their journey through the pipeline
     */
    List<CandidateStageHistory> findByCandidateIdOrderByChangedAtAsc(Long candidateId);
    
    /**
     * Find recent stage transitions
     * For dashboard "Recent Activity" widget
     */
    @Query("SELECT h FROM CandidateStageHistory h " +
           "WHERE h.changedAt >= :since " +
           "ORDER BY h.changedAt DESC")
    List<CandidateStageHistory> findRecentStageChanges(@Param("since") LocalDateTime since);
    
    /**
     * Calculate average time in each stage
     * Business intelligence: Where do candidates get stuck?
     */
    @Query(value = 
        "WITH stage_durations AS (" +
        "    SELECT " +
        "        from_stage, " +
        "        AVG(EXTRACT(EPOCH FROM (changed_at - LAG(changed_at) OVER (PARTITION BY candidate_id ORDER BY changed_at))) / 86400) AS avg_days " +
        "    FROM candidate_stage_history " +
        "    GROUP BY from_stage" +
        ") " +
        "SELECT from_stage, avg_days FROM stage_durations",
        nativeQuery = true
    )
    List<Object[]> getAverageTimeInEachStage();
    
    /**
     * Find candidates who moved to a specific stage today
     */
    @Query("SELECT h FROM CandidateStageHistory h " +
           "WHERE h.toStage = :stage " +
           "AND CAST(h.changedAt AS date) = CAST(:today AS date)")
    List<CandidateStageHistory> findCandidatesMovedToStageToday(
        @Param("stage") CandidateStage stage,
        @Param("today") LocalDateTime today
    );
}

/**
 * REPOSITORY BEST PRACTICES:
 * 
 * 1. Always use @Repository annotation for exception translation
 * 2. Use Optional<T> for single results that might not exist
 * 3. Use List<T> for multiple results (never return null)
 * 4. Use Page<T> for large result sets (with Pageable parameter)
 * 5. Name methods clearly - findBy, existsBy, countBy, deleteBy
 * 6. Use @Query for complex logic that can't be expressed in method names
 * 7. Use native SQL only when JPQL isn't enough
 * 8. Always add ORDER BY for predictable results
 * 9. Use JOIN FETCH carefully to avoid N+1 problems
 * 10. Index frequently queried columns (done in @Table(indexes=...))
 */