package com.interview.management.repository;

import com.interview.management.entity.Candidate;
import com.interview.management.entity.enums.CandidateStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 * Candidate Repository
 * 
 * Spring Data JPA provides:
 * - Basic CRUD: save(), findById(), findAll(), delete(), etc.
 * - We add custom methods for business logic
 * 
 * Method Naming Convention:
 * - findBy{Property} → Automatically generates query
 * - Custom queries using @Query annotation
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    // ============================================
    // AUTOMATIC QUERY METHODS (Method Naming)
    // ============================================
    
    /**
     * Find candidate by email
     * Spring generates: SELECT * FROM candidates WHERE email = ?
     * 
     * Naming convention: findBy + PropertyName
     */
    Optional<Candidate> findByEmail(String email);
    
    /**
     * Check if email already exists
     * Spring generates: SELECT COUNT(*) > 0 FROM candidates WHERE email = ?
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all candidates in a specific stage
     * Spring generates: SELECT * FROM candidates WHERE current_stage = ?
     */
    List<Candidate> findByCurrentStage(CandidateStage stage);
    
    /**
     * Find candidates by stage with pagination
     * For large result sets, always use pagination!
     */
    Page<Candidate> findByCurrentStage(CandidateStage stage, Pageable pageable);
    
    /**
     * Find candidates whose name contains a string (case-insensitive)
     * Spring generates: SELECT * FROM candidates WHERE LOWER(name) LIKE LOWER(?%)
     */
    List<Candidate> findByNameContainingIgnoreCase(String nameFragment);
    
    /**
     * Find candidates created after a certain date
     */
    List<Candidate> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Complex method naming: multiple conditions with AND/OR
     * Find by stage AND created after date
     */
    List<Candidate> findByCurrentStageAndCreatedAtAfter(
        CandidateStage stage, 
        LocalDateTime date
    );
    
    // ============================================
    // CUSTOM JPQL QUERIES
    // ============================================
    
    /**
     * DASHBOARD: Count candidates by stage
     * Returns Map: {APPLIED=12, SCREENING=8, HIRED=3, ...}
     * 
     * Why custom query? Method naming can't do GROUP BY
     */
    @Query("SELECT c.currentStage, COUNT(c) FROM Candidate c GROUP BY c.currentStage")
    List<Object[]> countByStage();
    
    /**
     * Better version using Map projection
     * Returns a clean Map instead of Object[]
     */
    @Query("SELECT c.currentStage AS stage, COUNT(c) AS count " +
           "FROM Candidate c " +
           "GROUP BY c.currentStage")
    List<Map<String, Object>> countCandidatesByStage();
    
    /**
     * DASHBOARD: Total candidates this month
     * 
     * :startDate is a named parameter (safer than ?1, ?2)
     */
    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.createdAt >= :startDate")
    Long countCandidatesCreatedAfter(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Find candidates with their latest AI screening score
     * Uses JOIN to get related data efficiently
     * 
     * FETCH keyword: Eager load the relationship (avoid N+1 problem)
     */
    @Query("SELECT DISTINCT c FROM Candidate c " +
           "LEFT JOIN FETCH c.screenings s " +
           "WHERE c.currentStage = :stage " +
           "ORDER BY s.matchScore DESC")
    List<Candidate> findCandidatesWithScreeningByStage(@Param("stage") CandidateStage stage);
    
    /**
     * Find candidates who haven't been screened yet
     * Using NOT EXISTS subquery
     */
    @Query("SELECT c FROM Candidate c WHERE NOT EXISTS " +
           "(SELECT 1 FROM AIScreening s WHERE s.candidate = c)")
    List<Candidate> findCandidatesWithoutScreening();
    
    /**
     * Search candidates by multiple criteria
     * Dynamic search - all parameters are optional
     */
    @Query("SELECT c FROM Candidate c WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:stage IS NULL OR c.currentStage = :stage)")
    Page<Candidate> searchCandidates(
        @Param("name") String name,
        @Param("email") String email,
        @Param("stage") CandidateStage stage,
        Pageable pageable
    );
    
    // ============================================
    // NATIVE SQL QUERIES (For Complex Operations)
    // ============================================
    
    /**
     * DASHBOARD: Advanced analytics using native SQL
     * When JPQL becomes too complex, use native SQL
     * 
     * nativeQuery = true: Use actual SQL syntax
     */
    @Query(value = 
        "SELECT " +
        "    current_stage AS stage, " +
        "    COUNT(*) AS total, " +
        "    AVG(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - created_at)) / 86400) AS avg_days_in_stage " +
        "FROM candidates " +
        "GROUP BY current_stage",
        nativeQuery = true
    )
    List<Map<String, Object>> getCandidateStageAnalytics();
    
    /**
     * Find candidates with highest AI match scores
     * Joins candidates with their best screening score
     */
    @Query(value = 
        "SELECT c.* FROM candidates c " +
        "INNER JOIN (" +
        "    SELECT candidate_id, MAX(match_score) as max_score " +
        "    FROM ai_screenings " +
        "    GROUP BY candidate_id" +
        ") s ON c.id = s.candidate_id " +
        "WHERE s.max_score >= :minScore " +
        "ORDER BY s.max_score DESC " +
        "LIMIT :limit",
        nativeQuery = true
    )
    List<Candidate> findTopCandidatesByAIScore(
        @Param("minScore") int minScore,
        @Param("limit") int limit
    );
    
    /**
     * PERFORMANCE TIP: Use projections for read-only data
     * Instead of loading full entity, load only needed fields
     */
    @Query("SELECT c.id AS id, c.name AS name, c.email AS email, " +
           "c.currentStage AS stage, c.createdAt AS appliedDate " +
           "FROM Candidate c " +
           "WHERE c.currentStage IN :stages")
    List<CandidateSummaryProjection> findCandidateSummaries(@Param("stages") List<CandidateStage> stages);
    
    /**
     * Projection interface for lightweight queries
     * Spring automatically implements this at runtime
     */
    interface CandidateSummaryProjection {
        Long getId();
        String getName();
        String getEmail();
        CandidateStage getStage();
        LocalDateTime getAppliedDate();
    }
}

/**
 * KEY CONCEPTS TO REMEMBER:
 * 
 * 1. METHOD NAMING CONVENTIONS:
 *    - findBy{Property}
 *    - findBy{Property}And{Property}
 *    - findBy{Property}Or{Property}
 *    - findBy{Property}Containing
 *    - findBy{Property}IgnoreCase
 *    - exists/count/delete + By{Property}
 * 
 * 2. WHEN TO USE @Query:
 *    - Complex joins
 *    - Aggregations (GROUP BY, COUNT, SUM)
 *    - Subqueries
 *    - Custom projections
 * 
 * 3. JPQL vs NATIVE SQL:
 *    - JPQL: Works with entities (database-independent)
 *    - Native SQL: Works with tables (database-specific but more powerful)
 * 
 * 4. PERFORMANCE:
 *    - Always use pagination for large results
 *    - Use projections for read-only queries
 *    - Be careful with JOIN FETCH (can cause N+1 problem if misused)
 */