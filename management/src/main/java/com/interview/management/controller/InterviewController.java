package com.interview.management.controller;

import com.interview.management.entity.Interview;
import com.interview.management.entity.enums.InterviewStatus;
import com.interview.management.entity.enums.InterviewType;
import com.interview.management.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interview REST Controller
 * 
 * Endpoints for interview scheduling and management
 */
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InterviewController {
    
    private final InterviewService interviewService;
    
    /**
     * POST /api/interviews - Schedule new interview
     */
    @PostMapping
    public ResponseEntity<Interview> scheduleInterview(
            @RequestParam Long candidateId,
            @RequestParam Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam InterviewType type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes,
            @RequestParam String scheduledBy) {
        
        log.info("POST /api/interviews - Scheduling interview");
        
        try {
            Interview interview = interviewService.scheduleInterview(
                    candidateId, interviewerId, scheduledAt, durationMinutes,
                    type, location, notes, scheduledBy);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(interview);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("Scheduling conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error scheduling interview", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/interviews/{id} - Get interview by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Interview> getInterviewById(@PathVariable Long id) {
        log.info("GET /api/interviews/{}", id);
        
        try {
            Interview interview = interviewService.getInterviewById(id);
            return ResponseEntity.ok(interview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/interviews/candidate/{candidateId} - Get interviews for candidate
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Interview>> getInterviewsForCandidate(@PathVariable Long candidateId) {
        log.info("GET /api/interviews/candidate/{}", candidateId);
        
        List<Interview> interviews = interviewService.getInterviewsForCandidate(candidateId);
        return ResponseEntity.ok(interviews);
    }
    
    /**
     * GET /api/interviews/interviewer/{interviewerId} - Get interviews for interviewer
     */
    @GetMapping("/interviewer/{interviewerId}")
    public ResponseEntity<List<Interview>> getInterviewsForInterviewer(@PathVariable Long interviewerId) {
        log.info("GET /api/interviews/interviewer/{}", interviewerId);
        
        List<Interview> interviews = interviewService.getInterviewsForInterviewer(interviewerId);
        return ResponseEntity.ok(interviews);
    }
    
    /**
     * GET /api/interviews/today - Get today's interviews
     */
    @GetMapping("/today")
    public ResponseEntity<List<Interview>> getTodaysInterviews() {
        log.info("GET /api/interviews/today");
        
        List<Interview> interviews = interviewService.getTodaysInterviews();
        return ResponseEntity.ok(interviews);
    }
    
    /**
     * GET /api/interviews/candidate/{candidateId}/upcoming - Get upcoming interviews
     */
    @GetMapping("/candidate/{candidateId}/upcoming")
    public ResponseEntity<List<Interview>> getUpcomingInterviews(@PathVariable Long candidateId) {
        log.info("GET /api/interviews/candidate/{}/upcoming", candidateId);
        
        List<Interview> interviews = interviewService.getUpcomingInterviews(candidateId);
        return ResponseEntity.ok(interviews);
    }
    
    /**
     * GET /api/interviews/date-range - Get interviews in date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Interview>> getInterviewsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        log.info("GET /api/interviews/date-range - start: {}, end: {}", start, end);
        
        List<Interview> interviews = interviewService.getInterviewsInDateRange(start, end);
        return ResponseEntity.ok(interviews);
    }
    
    /**
     * GET /api/interviews/status/{status} - Get interviews by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Interview>> getInterviewsByStatus(@PathVariable InterviewStatus status) {
        log.info("GET /api/interviews/status/{}", status);
        
        List<Interview> interviews = interviewService.getInterviewsByStatus(status);
        return ResponseEntity.ok(interviews);
    }
    
    /**
     * GET /api/interviews/pending-feedback - Get completed interviews without feedback
     */
    @GetMapping("/pending-feedback")
    public ResponseEntity<List<Interview>> getCompletedInterviewsWithoutFeedback() {
        log.info("GET /api/interviews/pending-feedback");
        
        List<Interview> interviews = interviewService.getCompletedInterviewsWithoutFeedback();
        return ResponseEntity.ok(interviews);
    }
    
    /**
     * GET /api/interviews/overdue - Get overdue interviews
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Interview>> getOverdueInterviews() {
        log.info("GET /api/interviews/overdue");
        
        List<Interview> interviews = interviewService.getOverdueInterviews();
        return ResponseEntity.ok(interviews);
    }
    
    /**
     * PUT /api/interviews/{id}/status - Update interview status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Interview> updateInterviewStatus(
            @PathVariable Long id,
            @RequestParam InterviewStatus newStatus,
            @RequestParam String changedBy,
            @RequestParam(required = false) String notes) {
        
        log.info("PUT /api/interviews/{}/status - Updating to {}", id, newStatus);
        
        try {
            Interview updated = interviewService.updateInterviewStatus(id, newStatus, changedBy, notes);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Error updating status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating interview status", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * PUT /api/interviews/{id}/reschedule - Reschedule interview
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Interview> rescheduleInterview(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newScheduledAt,
            @RequestParam(required = false) Integer newDuration,
            @RequestParam String rescheduledBy,
            @RequestParam(required = false) String reason) {
        
        log.info("PUT /api/interviews/{}/reschedule - New time: {}", id, newScheduledAt);
        
        try {
            Interview updated = interviewService.rescheduleInterview(
                    id, newScheduledAt, newDuration, rescheduledBy, reason);
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error rescheduling: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error rescheduling interview", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * PUT /api/interviews/{id}/cancel - Cancel interview
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Interview> cancelInterview(
            @PathVariable Long id,
            @RequestParam String cancelledBy,
            @RequestParam(required = false) String reason) {
        
        log.info("PUT /api/interviews/{}/cancel", id);
        
        try {
            Interview updated = interviewService.cancelInterview(id, cancelledBy, reason);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            log.error("Error cancelling: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error cancelling interview", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * PUT /api/interviews/{id} - Update interview details
     */
    @PutMapping("/{id}")
    public ResponseEntity<Interview> updateInterview(
            @PathVariable Long id,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes) {
        
        log.info("PUT /api/interviews/{} - Updating details", id);
        
        try {
            Interview updated = interviewService.updateInterview(id, location, notes);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * DELETE /api/interviews/{id} - Delete interview
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        log.info("DELETE /api/interviews/{}", id);
        
        try {
            interviewService.deleteInterview(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}