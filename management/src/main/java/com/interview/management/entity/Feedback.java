package com.interview.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.interview.management.entity.enums.Recommendation;

import java.time.LocalDateTime;
/**
 * Feedback Entity
 * Stores interviewer's assessment after interview
 * 
 * Design Notes:
 * - Multiple interviewers can provide feedback for same interview (panel)
 * - Structured ratings + free-form comments
 * - Final recommendation enum for consistency
 */
@Entity
@Table(name = "feedbacks", indexes = {
    @Index(name = "idx_feedback_interview", columnList = "interview_id"),
    @Index(name = "idx_feedback_interviewer", columnList = "interviewer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private Interviewer interviewer;
    
    /**
     * Rating scales (1-5)
     * Makes it easy to calculate average scores
     */
    @Column(name = "technical_score", nullable = false)
    private Integer technicalScore; // 1-5
    
    @Column(name = "communication_score", nullable = false)
    private Integer communicationScore; // 1-5
    
    @Column(name = "problem_solving_score", nullable = false)
    private Integer problemSolvingScore; // 1-5
    
    @Column(name = "cultural_fit_score")
    private Integer culturalFitScore; // 1-5, optional
    
    /**
     * Detailed comments
     */
    @Column(columnDefinition = "TEXT")
    private String strengths;
    
    @Column(columnDefinition = "TEXT")
    private String weaknesses;
    
    @Column(name = "additional_comments", columnDefinition = "TEXT")
    private String additionalComments;
    
    /**
     * Final recommendation
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Recommendation recommendation;
    
    /**
     * When feedback was submitted
     */
    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;
    
    /**
     * Calculate overall score (average of all ratings)
     */
    public Double getOverallScore() {
        int count = 3; // technical, communication, problem-solving
        int total = technicalScore + communicationScore + problemSolvingScore;
        
        if (culturalFitScore != null) {
            total += culturalFitScore;
            count++;
        }
        
        return (double) total / count;
    }
}