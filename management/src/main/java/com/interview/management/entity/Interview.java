package com.interview.management.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.interview.management.entity.enums.InterviewStatus;
import com.interview.management.entity.enums.InterviewType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Interview Entity
 * Represents a scheduled interview session
 * 
 * Key Features:
 * - Conflict detection through scheduledAt
 * - Status tracking through separate history table
 * - One-to-many with Feedback (multiple interviewers can provide feedback)
 */
@Entity
@Table(name = "interviews", indexes = {
    @Index(name = "idx_interview_candidate", columnList = "candidate_id"),
    @Index(name = "idx_interview_interviewer", columnList = "interviewer_id"),
    @Index(name = "idx_interview_scheduled", columnList = "scheduled_at"),
    @Index(name = "idx_interview_status", columnList = "current_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_id", nullable = false)
    private Interviewer interviewer;
    
    /**
     * When the interview is scheduled
     * Used for conflict detection
     */
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    
    /**
     * Interview duration in minutes
     * Default: 60 minutes
     */
    @Column(name = "duration_minutes")
    @Builder.Default
    private Integer durationMinutes = 60;
    
    /**
     * Current status - denormalized for performance
     * Full history in InterviewStatusHistory
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    @Builder.Default
    private InterviewStatus currentStatus = InterviewStatus.SCHEDULED;
    
    /**
     * Type of interview
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false)
    private InterviewType interviewType;
    
    /**
     * Location or meeting link
     * Example: "Conference Room 3A" or "https://meet.google.com/xyz"
     */
    @Column(length = 500)
    private String location;
    
    /**
     * Additional notes for the interview
     */
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    /**
     * Status change history
     */
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InterviewStatusHistory> statusHistory = new ArrayList<>();
    
    /**
     * Feedback from interviewers
     * One interview can have multiple feedback entries (panel interviews)
     */
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Feedback> feedbacks = new ArrayList<>();
    
    /**
     * Audit fields
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Helper method to update status and track history
     */
    public void updateStatus(InterviewStatus newStatus, String changedBy, String notes) {
        InterviewStatus oldStatus = this.currentStatus;
        this.currentStatus = newStatus;
        
        InterviewStatusHistory history = InterviewStatusHistory.builder()
            .interview(this)
            .status(newStatus)
            .changedBy(changedBy)
            .notes(notes)
            .build();
        
        statusHistory.add(history);
    }
    
    /**
     * Helper method to add feedback
     */
    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
        feedback.setInterview(this);
    }
    
    /**
     * Check if this interview overlaps with another scheduled time
     * Used for conflict detection
     */
    public boolean overlapsWith(LocalDateTime otherStart, Integer otherDurationMinutes) {
        LocalDateTime thisEnd = this.scheduledAt.plusMinutes(this.durationMinutes);
        LocalDateTime otherEnd = otherStart.plusMinutes(otherDurationMinutes);
        
        // Overlap if: (thisStart < otherEnd) AND (thisEnd > otherStart)
        return this.scheduledAt.isBefore(otherEnd) && thisEnd.isAfter(otherStart);
    }
}