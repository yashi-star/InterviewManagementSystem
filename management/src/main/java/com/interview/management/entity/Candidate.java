package com.interview.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.interview.management.entity.enums.CandidateStage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Candidate Entity - Represents a job applicant
 * 
 * Key Design Decisions:
 * 1. Email is unique - prevents duplicate candidates
 * 2. currentStage stored here for fast dashboard queries (denormalization)
 * 3. OneToMany with cascade for related entities
 * 4. Audit fields (createdAt, updatedAt) managed by Hibernate
 */
@Entity
@Table(name = "candidates", indexes = {
    @Index(name = "idx_candidate_email", columnList = "email"),
    @Index(name = "idx_candidate_stage", columnList = "current_stage")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    /**
     * Path to stored resume file
     * Example: "/resumes/1_john_doe.pdf"
     */
    @Column(name = "resume_path", length = 255)
    private String resumePath;
    
    /**
     * Current pipeline stage
     * Denormalized for performance - also tracked in CandidateStageHistory
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false)
    @Builder.Default
    private CandidateStage currentStage = CandidateStage.APPLIED;
    
    /**
     * One candidate can have multiple AI screening reports
     * (e.g., re-screening after resume update)
     */
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AIScreening> screenings = new ArrayList<>();
    
    /**
     * One candidate can have multiple interviews
     */
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Interview> interviews = new ArrayList<>();
    
    /**
     * Track stage transitions
     */
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CandidateStageHistory> stageHistory = new ArrayList<>();
    
    /**
     * Audit fields - automatically managed by Hibernate
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Helper method to add AI screening
     */
    public void addScreening(AIScreening screening) {
        screenings.add(screening);
        screening.setCandidate(this);
    }
    
    /**
     * Helper method to add interview
     */
    public void addInterview(Interview interview) {
        interviews.add(interview);
        interview.setCandidate(this);
    }
    
    /**
     * Helper method to update stage and track history
     */
    public void updateStage(CandidateStage newStage, String changedBy, String reason) {
        CandidateStage oldStage = this.currentStage;
        this.currentStage = newStage;
        
        // Create history record
        CandidateStageHistory history = CandidateStageHistory.builder()
            .candidate(this)
            .fromStage(oldStage)
            .toStage(newStage)
            .changedBy(changedBy)
            .reason(reason)
            .build();
        
        stageHistory.add(history);
    }
}