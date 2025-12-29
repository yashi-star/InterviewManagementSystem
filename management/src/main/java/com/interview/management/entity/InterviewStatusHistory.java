package com.interview.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.interview.management.entity.enums.InterviewStatus;

import java.time.LocalDateTime;

/**
 * Interview Status History
 * Tracks every status change of an interview
 * 
 * Why separate table?
 * - Audit trail: WHO changed WHAT and WHEN
 * - Compliance: required in many organizations
 * - Analytics: time spent in each status
 */
@Entity
@Table(name = "interview_status_history", indexes = {
    @Index(name = "idx_status_history_interview", columnList = "interview_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;
    
    /**
     * New status after change
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;
    
    /**
     * Who made this change
     * Example: email or user ID
     */
    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;
    
    /**
     * When the change happened
     */
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
    
    /**
     * Optional notes about why status changed
     */
    @Column(columnDefinition = "TEXT")
    private String notes;
}

