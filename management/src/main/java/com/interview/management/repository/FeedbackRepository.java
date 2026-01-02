package com.interview.management.repository;

import com.interview.management.entity.Feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * Find all feedback for an interview
     */
    List<Feedback> findByInterviewId(Long interviewId);
    
    /**
     * Find all feedback given by an interviewer
     */
    List<Feedback> findByInterviewerId(Long interviewerId);
    
    /**
     * Get average scores for a candidate (across all their interviews)
     */
    @Query("SELECT AVG(f.technicalScore), AVG(f.communicationScore), " +
           "AVG(f.problemSolvingScore) " +
           "FROM Feedback f " +
           "JOIN f.interview i " +
           "WHERE i.candidate.id = :candidateId")
    List<Object[]> getAverageScoresForCandidate(@Param("candidateId") Long candidateId);
    
    /**
     * Find feedback with high scores (STRONG_HIRE or HIRE)
     */
    @Query("SELECT f FROM Feedback f WHERE " +
           "f.recommendation IN ('STRONG_HIRE', 'HIRE') " +
           "ORDER BY f.submittedAt DESC")
    List<Feedback> findPositiveFeedback();
    
    /**
     * Get feedback statistics for an interviewer
     * Shows how harsh/lenient they are
     */
    @Query("SELECT AVG(f.technicalScore), AVG(f.communicationScore), " +
           "COUNT(f), SUM(CASE WHEN f.recommendation = 'STRONG_HIRE' THEN 1 ELSE 0 END) " +
           "FROM Feedback f " +
           "WHERE f.interviewer.id = :interviewerId")
    Object[] getInterviewerStatistics(@Param("interviewerId") Long interviewerId);
}
