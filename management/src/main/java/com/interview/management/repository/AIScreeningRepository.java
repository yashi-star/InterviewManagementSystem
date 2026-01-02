package com.interview.management.repository;

import com.interview.management.entity.AIScreening;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface AIScreeningRepository extends JpaRepository<AIScreening, Long> {
    
    /**
     * Find all screenings for a candidate
     */
    List<AIScreening> findByCandidateIdOrderByScreenedAtDesc(Long candidateId);
    
    /**
     * Get latest screening for a candidate
     * Useful when candidate updates resume and gets re-screened
     */
    @Query("SELECT s FROM AIScreening s WHERE s.candidate.id = :candidateId " +
           "ORDER BY s.screenedAt DESC LIMIT 1")
    Optional<AIScreening> findLatestScreeningForCandidate(@Param("candidateId") Long candidateId);
    
    /**
     * Find candidates with high match scores
     * Used to identify top talent
     */
    @Query("SELECT s FROM AIScreening s WHERE s.matchScore >= :minScore " +
           "ORDER BY s.matchScore DESC")
    List<AIScreening> findHighScoreCandidates(@Param("minScore") int minScore);
    
    /**
     * Get average AI match score by candidate stage
     * Analytics: Are screened candidates actually getting hired?
     */
    @Query("SELECT c.currentStage, AVG(s.matchScore) " +
           "FROM AIScreening s " +
           "JOIN s.candidate c " +
           "GROUP BY c.currentStage")
    List<Object[]> getAverageScoreByStage();
    
    /**
     * Find screenings done in a date range
     */
    @Query("SELECT s FROM AIScreening s WHERE s.screenedAt BETWEEN :startDate AND :endDate")
    List<AIScreening> findScreeningsInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
