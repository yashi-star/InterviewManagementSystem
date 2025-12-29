package com.interview.management.entity;

import jakarta.persistence.*;
import lombok.*;
import com.interview.management.entity.enums.CandidateStage;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_stage_history", indexes = {
    @Index(name = "idx_stage_history_candidate", columnList = "candidate_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CandidateStageHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    /**
     * Previous stage
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_stage", nullable = false)
    private CandidateStage fromStage;
    
    /**
     * New stage
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_stage", nullable = false)
    private CandidateStage toStage;
    
    /**
     * Who made this change
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
     * Reason for stage change
     */
    @Column(columnDefinition = "TEXT")
    private String reason;
}