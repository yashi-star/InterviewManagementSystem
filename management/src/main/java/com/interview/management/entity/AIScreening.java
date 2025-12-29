package com.interview.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;


/**
 * AI Screening Report
 * Stores results from AI-powered resume analysis
 * 
 * Key Design:
 * - JSON column for flexible skill storage
 * - Separate from Candidate to allow re-screening
 * - Stores both structured data and free-form analysis
 */
@Entity
@Table(name = "ai_screenings", indexes = {
    @Index(name = "idx_screening_candidate", columnList = "candidate_id"),
    @Index(name = "idx_screening_score", columnList = "match_score")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIScreening {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    /**
     * Skills extracted and matched from resume
     * Stored as JSON for flexibility
     * Example: [{"skill": "Java", "proficiency": "Advanced", "years": 3}]
     * 
     * Note: For simplicity, we'll store this as TEXT and parse manually
     * In production, consider using JSON column type with Hibernate JSON support
     */
    @Column(name = "skills_matched", columnDefinition = "TEXT")
    private String skillsMatched;
    
    /**
     * Total years of professional experience
     */
    @Column(name = "experience_years")
    private Double experienceYears;
    
    /**
     * Education level extracted
     * Example: "B.Tech Computer Science, XYZ University, 2020"
     */
    @Column(name = "education_level", length = 500)
    private String educationLevel;
    
    /**
     * Cultural fit indicators
     * JSON string with teamwork mentions, leadership, etc.
     */
    @Column(name = "cultural_fit", columnDefinition = "TEXT")
    private String culturalFit;
    
    /**
     * Overall match score (0-100)
     * Used for ranking candidates
     */
    @Column(name = "match_score", nullable = false)
    private Integer matchScore;
    
    /**
     * Free-form AI analysis text
     * The AI's summary and recommendation
     */
    @Column(name = "analysis_text", columnDefinition = "TEXT")
    private String analysisText;
    
    /**
     * When this screening was performed
     */
    @CreationTimestamp
    @Column(name = "screened_at", nullable = false, updatable = false)
    private LocalDateTime screenedAt;
    
    /**
     * AI model used for screening
     * Example: "llama2", "gpt-4"
     */
    @Column(name = "ai_model_used", length = 50)
    private String aiModelUsed;
}