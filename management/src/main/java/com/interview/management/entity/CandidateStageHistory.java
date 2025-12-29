package com.interview.management.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.interview.management.entity.enums.CandidateStage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "candidate_stage_history", indexes = {
    @Index(name = "idx_stage_history_candidate", columnList = "candidate_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateStageHistory {
    
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