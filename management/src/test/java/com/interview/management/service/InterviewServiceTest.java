package com.interview.management.service;

import com.interview.management.entity.Candidate;
import com.interview.management.entity.Interview;
import com.interview.management.entity.Interviewer;
import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.entity.enums.InterviewStatus;
import com.interview.management.entity.enums.InterviewType;
import com.interview.management.exception.ResourceNotFoundException;
import com.interview.management.exception.SchedulingConflictException;
import com.interview.management.exception.ValidationException;
import com.interview.management.repository.CandidateRepository;
import com.interview.management.repository.InterviewRepository;
import com.interview.management.repository.InterviewerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InterviewService
 * Focus on scheduling logic and conflict detection
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewService Unit Tests")
class InterviewServiceTest {
    
    @Mock
    private InterviewRepository interviewRepository;
    
    @Mock
    private CandidateRepository candidateRepository;
    
    @Mock
    private InterviewerRepository interviewerRepository;
    
    @InjectMocks
    private InterviewService interviewService;
    
    private Candidate testCandidate;
    private Interviewer testInterviewer;
    private Interview testInterview;
    private LocalDateTime futureTime;
    
    @BeforeEach
    void setUp() {
        futureTime = LocalDateTime.now().plusDays(1);
        
        testCandidate = Candidate.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .currentStage(CandidateStage.SCREENING)
                .build();
        
        testInterviewer = Interviewer.builder()
                .id(1L)
                .name("Jane Smith")
                .email("jane@company.com")
                .build();
        
        testInterview = Interview.builder()
                .id(1L)
                .candidate(testCandidate)
                .interviewer(testInterviewer)
                .scheduledAt(futureTime)
                .durationMinutes(60)
                .currentStatus(InterviewStatus.SCHEDULED)
                .interviewType(InterviewType.TECHNICAL)
                .build();
    }
    
    // ============================================
    // SCHEDULE INTERVIEW TESTS
    // ============================================
    
    @Test
    @DisplayName("Should schedule interview successfully")
    void scheduleInterview_Success() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
        when(interviewRepository.findByInterviewerIdAndScheduledAtBetween(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);
        
        // When
        Interview result = interviewService.scheduleInterview(
                1L, 1L, futureTime, 60, 
                InterviewType.TECHNICAL, "Room A", "Technical round", "recruiter"
        );
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentStatus()).isEqualTo(InterviewStatus.SCHEDULED);
        
        verify(candidateRepository).findById(1L);
        verify(interviewerRepository).findById(1L);
        verify(interviewRepository).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when candidate not found")
    void scheduleInterview_CandidateNotFound_ThrowsException() {
        // Given
        when(candidateRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> interviewService.scheduleInterview(
                999L, 1L, futureTime, 60, 
                InterviewType.TECHNICAL, "Room A", "test", "recruiter"
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Candidate");
        
        verify(candidateRepository).findById(999L);
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when interviewer not found")
    void scheduleInterview_InterviewerNotFound_ThrowsException() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(interviewerRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> interviewService.scheduleInterview(
                1L, 999L, futureTime, 60, 
                InterviewType.TECHNICAL, "Room A", "test", "recruiter"
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Interviewer");
        
        verify(interviewerRepository).findById(999L);
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when scheduling in the past")
    void scheduleInterview_PastDate_ThrowsException() {
        // Given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.scheduleInterview(
                1L, 1L, pastTime, 60, 
                InterviewType.TECHNICAL, "Room A", "test", "recruiter"
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("past");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when candidate in wrong stage")
    void scheduleInterview_WrongStage_ThrowsException() {
        // Given
        testCandidate.setCurrentStage(CandidateStage.APPLIED);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.scheduleInterview(
                1L, 1L, futureTime, 60, 
                InterviewType.TECHNICAL, "Room A", "test", "recruiter"
        ))
                .hasMessageContaining("screening");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when invalid duration")
    void scheduleInterview_InvalidDuration_ThrowsException() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
        
        // When & Then - duration too short
        assertThatThrownBy(() -> interviewService.scheduleInterview(
                1L, 1L, futureTime, 10, // 10 minutes - too short
                InterviewType.TECHNICAL, "Room A", "test", "recruiter"
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("duration");
        
        // When & Then - duration too long
        assertThatThrownBy(() -> interviewService.scheduleInterview(
                1L, 1L, futureTime, 500, // 500 minutes - too long
                InterviewType.TECHNICAL, "Room A", "test", "recruiter"
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("duration");
    }
    
    // ============================================
    // CONFLICT DETECTION TESTS
    // ============================================
    
    @Test
    @DisplayName("Should detect scheduling conflict")
    void scheduleInterview_Conflict_ThrowsException() {
        // Given
        Interview existingInterview = Interview.builder()
                .id(2L)
                .interviewer(testInterviewer)
                .scheduledAt(futureTime)
                .durationMinutes(60)
                .currentStatus(InterviewStatus.SCHEDULED)
                .build();
        
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
        when(interviewRepository.findByInterviewerIdAndScheduledAtBetween(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(existingInterview));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.scheduleInterview(
                1L, 1L, futureTime.plusMinutes(30), 60, // Overlaps with existing
                InterviewType.TECHNICAL, "Room A", "test", "recruiter"
        ))
                .isInstanceOf(SchedulingConflictException.class)
                .hasMessageContaining("conflict");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should not detect conflict when times don't overlap")
    void scheduleInterview_NoConflict_Success() {
        // Given
        Interview existingInterview = Interview.builder()
                .id(2L)
                .interviewer(testInterviewer)
                .scheduledAt(futureTime)
                .durationMinutes(60)
                .currentStatus(InterviewStatus.SCHEDULED)
                .build();
        
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(interviewerRepository.findById(1L)).thenReturn(Optional.of(testInterviewer));
        when(interviewRepository.findByInterviewerIdAndScheduledAtBetween(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(existingInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);
        
        // When - schedule 2 hours later (no overlap)
        LocalDateTime nonConflictingTime = futureTime.plusHours(2);
        Interview result = interviewService.scheduleInterview(
                1L, 1L, nonConflictingTime, 60, 
                InterviewType.TECHNICAL, "Room A", "test", "recruiter"
        );
        
        // Then
        assertThat(result).isNotNull();
        verify(interviewRepository).save(any(Interview.class));
    }
    
    // ============================================
    // UPDATE STATUS TESTS
    // ============================================
    
    @Test
    @DisplayName("Should update interview status successfully")
    void updateInterviewStatus_Success() {
        // Given
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);
        
        // When
        Interview result = interviewService.updateInterviewStatus(
                1L, InterviewStatus.COMPLETED, "interviewer", "Interview completed"
        );
        
        // Then
        assertThat(result).isNotNull();
        verify(interviewRepository).findById(1L);
        verify(interviewRepository).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when status is already current")
    void updateInterviewStatus_SameStatus_ThrowsException() {
        // Given
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.updateInterviewStatus(
                1L, InterviewStatus.SCHEDULED, "interviewer", "test"
        ))
                .hasMessageContaining("already in status");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating completed interview")
    void updateInterviewStatus_CompletedInterview_ThrowsException() {
        // Given
        testInterview.setCurrentStatus(InterviewStatus.COMPLETED);
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.updateInterviewStatus(
                1L, InterviewStatus.CANCELLED, "interviewer", "test"
        ))
                .hasMessageContaining("status transition");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    // ============================================
    // RESCHEDULE TESTS
    // ============================================
    
    @Test
    @DisplayName("Should reschedule interview successfully")
    void rescheduleInterview_Success() {
        // Given
        LocalDateTime newTime = futureTime.plusDays(1);
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        when(interviewRepository.findByInterviewerIdAndScheduledAtBetween(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);
        
        // When
        Interview result = interviewService.rescheduleInterview(
                1L, newTime, 60, "recruiter", "Interviewer unavailable"
        );
        
        // Then
        assertThat(result).isNotNull();
        verify(interviewRepository).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when rescheduling completed interview")
    void rescheduleInterview_CompletedInterview_ThrowsException() {
        // Given
        testInterview.setCurrentStatus(InterviewStatus.COMPLETED);
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.rescheduleInterview(
                1L, futureTime.plusDays(1), 60, "recruiter", "test"
        ))
                .hasMessageContaining("Cannot reschedule completed");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when rescheduling to past")
    void rescheduleInterview_PastDate_ThrowsException() {
        // Given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.rescheduleInterview(
                1L, pastTime, 60, "recruiter", "test"
        ))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("past");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    // ============================================
    // CANCEL INTERVIEW TESTS
    // ============================================
    
    @Test
    @DisplayName("Should cancel interview successfully")
    void cancelInterview_Success() {
        // Given
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        when(interviewRepository.save(any(Interview.class))).thenReturn(testInterview);
        
        // When
        Interview result = interviewService.cancelInterview(
                1L, "recruiter", "Candidate withdrew"
        );
        
        // Then
        assertThat(result).isNotNull();
        verify(interviewRepository).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when cancelling completed interview")
    void cancelInterview_CompletedInterview_ThrowsException() {
        // Given
        testInterview.setCurrentStatus(InterviewStatus.COMPLETED);
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.cancelInterview(
                1L, "recruiter", "test"
        ))
                .hasMessageContaining("Cannot cancel completed");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
    
    @Test
    @DisplayName("Should throw exception when cancelling already cancelled interview")
    void cancelInterview_AlreadyCancelled_ThrowsException() {
        // Given
        testInterview.setCurrentStatus(InterviewStatus.CANCELLED);
        when(interviewRepository.findById(1L)).thenReturn(Optional.of(testInterview));
        
        // When & Then
        assertThatThrownBy(() -> interviewService.cancelInterview(
                1L, "recruiter", "test"
        ))
                .hasMessageContaining("already cancelled");
        
        verify(interviewRepository, never()).save(any(Interview.class));
    }
}