package com.interview.management.service;

import com.interview.management.entity.Candidate;
import com.interview.management.entity.Interview;
import com.interview.management.entity.Interviewer;
import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.entity.enums.InterviewStatus;
import com.interview.management.entity.enums.InterviewType;
import com.interview.management.exception.SchedulingConflictException;
import com.interview.management.repository.CandidateRepository;
import com.interview.management.repository.InterviewRepository;
import com.interview.management.repository.InterviewerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interview Service
 * Handles interview scheduling and management with conflict detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterviewService {
    
    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewerRepository interviewerRepository;
    
    /**
     * Schedule an interview with conflict detection
     */
    public Interview scheduleInterview(Long candidateId, Long interviewerId, 
                                       LocalDateTime scheduledAt, Integer durationMinutes,
                                       InterviewType type, String location, 
                                       String notes, String scheduledBy) {
        
        log.info("Scheduling interview - Candidate: {}, Interviewer: {}, Time: {}", 
                candidateId, interviewerId, scheduledAt);
        
        // Validate entities exist
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found: " + candidateId));
        
        Interviewer interviewer = interviewerRepository.findById(interviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Interviewer not found: " + interviewerId));
        
        // Check for scheduling conflicts
        checkForConflicts(interviewerId, scheduledAt, durationMinutes);
        
        // Create interview
        Interview interview = Interview.builder()
                .candidate(candidate)
                .interviewer(interviewer)
                .scheduledAt(scheduledAt)
                .durationMinutes(durationMinutes != null ? durationMinutes : 60)
                .currentStatus(InterviewStatus.SCHEDULED)
                .interviewType(type)
                .location(location)
                .notes(notes)
                .build();
        
        // Save interview
        Interview saved = interviewRepository.save(interview);
        
        // Update candidate stage to INTERVIEW_SCHEDULED if not already
        if (candidate.getCurrentStage() != CandidateStage.INTERVIEW_SCHEDULED &&
            candidate.getCurrentStage() != CandidateStage.INTERVIEW_COMPLETED) {
            candidate.updateStage(CandidateStage.INTERVIEW_SCHEDULED, 
                    scheduledBy, "Interview scheduled");
            candidateRepository.save(candidate);
        }
        
        log.info("Interview scheduled successfully with ID: {}", saved.getId());
        return saved;
    }
    
    /**
     * Check for scheduling conflicts
     */
    private void checkForConflicts(Long interviewerId, LocalDateTime scheduledAt, 
                                   Integer durationMinutes) {
        
        int duration = durationMinutes != null ? durationMinutes : 60;
        
        // Query for interviews in the time window (±2 hours buffer)
        LocalDateTime startWindow = scheduledAt.minusHours(2);
        LocalDateTime endWindow = scheduledAt.plusMinutes(duration).plusHours(2);
        
        List<Interview> existingInterviews = interviewRepository
                .findByInterviewerIdAndScheduledAtBetween(interviewerId, startWindow, endWindow);
        
        // Check for actual overlaps
        LocalDateTime proposedEnd = scheduledAt.plusMinutes(duration);
        for (Interview existing : existingInterviews) {
            if (existing.overlapsWith(scheduledAt, duration)) {
                String message = String.format(
                        "Scheduling conflict: Interviewer %d already has an interview at %s",
                        interviewerId, existing.getScheduledAt()
                );
                log.error(message);
                throw new SchedulingConflictException(message);
            }
        }
        
        log.debug("No scheduling conflicts found for interviewer {} at {}", 
                interviewerId, scheduledAt);
    }
    
    /**
     * Get interview by ID
     */
    @Transactional(readOnly = true)
    public Interview getInterviewById(Long id) {
        log.debug("Fetching interview: {}", id);
        return interviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + id));
    }
    
    /**
     * Get all interviews for a candidate
     */
    @Transactional(readOnly = true)
    public List<Interview> getInterviewsForCandidate(Long candidateId) {
        log.debug("Fetching interviews for candidate: {}", candidateId);
        return interviewRepository.findByCandidateIdOrderByScheduledAtDesc(candidateId);
    }
    
    /**
     * Get all interviews for an interviewer
     */
    @Transactional(readOnly = true)
    public List<Interview> getInterviewsForInterviewer(Long interviewerId) {
        log.debug("Fetching interviews for interviewer: {}", interviewerId);
        return interviewRepository.findByInterviewerIdOrderByScheduledAtDesc(interviewerId);
    }
    
    /**
     * Get today's interviews
     */
    @Transactional(readOnly = true)
    public List<Interview> getTodaysInterviews() {
        log.debug("Fetching today's interviews");
        return interviewRepository.findTodaysInterviews(LocalDateTime.now());
    }
    
    /**
     * Get upcoming interviews for a candidate
     */
    @Transactional(readOnly = true)
    public List<Interview> getUpcomingInterviews(Long candidateId) {
        log.debug("Fetching upcoming interviews for candidate: {}", candidateId);
        return interviewRepository.findUpcomingInterviewsForCandidate(
                candidateId, LocalDateTime.now());
    }
    
    /**
     * Get interviews in date range
     */
    @Transactional(readOnly = true)
    public List<Interview> getInterviewsInDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching interviews between {} and {}", start, end);
        return interviewRepository.findInterviewsInDateRange(start, end);
    }
    
    /**
     * Update interview status
     */
    public Interview updateInterviewStatus(Long interviewId, InterviewStatus newStatus, 
                                          String changedBy, String notes) {
        
        log.info("Updating interview {} status to {}", interviewId, newStatus);
        
        Interview interview = getInterviewById(interviewId);
        
        if (interview.getCurrentStatus() == newStatus) {
            throw new IllegalArgumentException("Interview is already in status: " + newStatus);
        }
        
        interview.updateStatus(newStatus, changedBy, notes);
        
        // Update candidate stage if interview completed
        if (newStatus == InterviewStatus.COMPLETED) {
            Candidate candidate = interview.getCandidate();
            if (candidate.getCurrentStage() != CandidateStage.INTERVIEW_COMPLETED) {
                candidate.updateStage(CandidateStage.INTERVIEW_COMPLETED, 
                        changedBy, "Interview completed");
                candidateRepository.save(candidate);
            }
        }
        
        Interview updated = interviewRepository.save(interview);
        log.info("Interview {} status updated to {}", interviewId, newStatus);
        return updated;
    }
    
    /**
     * Reschedule interview
     */
    public Interview rescheduleInterview(Long interviewId, LocalDateTime newScheduledAt, 
                                        Integer newDuration, String rescheduledBy, String reason) {
        
        log.info("Rescheduling interview: {}", interviewId);
        
        Interview interview = getInterviewById(interviewId);
        
        if (interview.getCurrentStatus() == InterviewStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reschedule completed interview");
        }
        
        // Check conflicts with new time
        int duration = newDuration != null ? newDuration : interview.getDurationMinutes();
        checkForConflicts(interview.getInterviewer().getId(), newScheduledAt, duration);
        
        // Update interview
        interview.setScheduledAt(newScheduledAt);
        if (newDuration != null) {
            interview.setDurationMinutes(newDuration);
        }
        interview.updateStatus(InterviewStatus.RESCHEDULED, rescheduledBy, reason);
        interview.setCurrentStatus(InterviewStatus.SCHEDULED); // Set back to SCHEDULED
        
        Interview updated = interviewRepository.save(interview);
        log.info("Interview {} rescheduled to {}", interviewId, newScheduledAt);
        return updated;
    }
    
    /**
     * Cancel interview
     */
    public Interview cancelInterview(Long interviewId, String cancelledBy, String reason) {
        log.info("Cancelling interview: {}", interviewId);
        
        Interview interview = getInterviewById(interviewId);
        
        if (interview.getCurrentStatus() == InterviewStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed interview");
        }
        
        interview.updateStatus(InterviewStatus.CANCELLED, cancelledBy, reason);
        
        Interview updated = interviewRepository.save(interview);
        log.info("Interview {} cancelled", interviewId);
        return updated;
    }
    
    /**
     * Update interview details
     */
    public Interview updateInterview(Long interviewId, String location, String notes) {
        log.info("Updating interview details: {}", interviewId);
        
        Interview interview = getInterviewById(interviewId);
        
        if (location != null) {
            interview.setLocation(location);
        }
        if (notes != null) {
            interview.setNotes(notes);
        }
        
        Interview updated = interviewRepository.save(interview);
        log.info("Interview {} updated", interviewId);
        return updated;
    }
    
    /**
     * Delete interview
     */
    public void deleteInterview(Long interviewId) {
        log.info("Deleting interview: {}", interviewId);
        
        Interview interview = getInterviewById(interviewId);
        interviewRepository.delete(interview);
        
        log.info("Interview {} deleted", interviewId);
    }
    
    /**
     * Get interviews by status
     */
    @Transactional(readOnly = true)
    public List<Interview> getInterviewsByStatus(InterviewStatus status) {
        log.debug("Fetching interviews with status: {}", status);
        return interviewRepository.findByCurrentStatus(status);
    }
    
    /**
     * Get completed interviews without feedback
     */
    @Transactional(readOnly = true)
    public List<Interview> getCompletedInterviewsWithoutFeedback() {
        log.debug("Fetching completed interviews without feedback");
        return interviewRepository.findCompletedInterviewsWithoutFeedback();
    }
    
    /**
     * Count pending feedbacks
     */
    @Transactional(readOnly = true)
    public Long countPendingFeedbacks() {
        return interviewRepository.countPendingFeedbacks();
    }
    
    /**
     * Count interviews scheduled today
     */
    @Transactional(readOnly = true)
    public Long countInterviewsScheduledToday() {
        return interviewRepository.countInterviewsScheduledToday(LocalDateTime.now());
    }
    
    /**
     * Get overdue interviews (scheduled in past, still in SCHEDULED status)
     */
    @Transactional(readOnly = true)
    public List<Interview> getOverdueInterviews() {
        log.debug("Fetching overdue interviews");
        return interviewRepository.findOverdueInterviews(LocalDateTime.now());
    }
}