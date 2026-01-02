package com.interview.management.controller;

import com.interview.management.entity.InterviewStatusHistory;
import com.interview.management.repository.InterviewStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interview Status History Controller
 * 
 * READ-ONLY endpoints for viewing interview status history
 * History records are created automatically in InterviewService, not manually
 */
@RestController
@RequestMapping("/api/history/interviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InterviewStageHistoryController {
    
    private final InterviewStatusHistoryRepository historyRepository;
    
    /**
     * GET /api/history/interviews/{interviewId} - Get complete status history for an interview
     * 
     * Use Case: Interview audit log
     * Shows: "Why was this interview rescheduled twice?"
     * 
     * Example Response:
     * [
     *   {status: "SCHEDULED", changedBy: "recruiter@company.com", changedAt: "2024-01-05T10:00:00", notes: "Initial scheduling"},
     *   {status: "RESCHEDULED", changedBy: "recruiter@company.com", changedAt: "2024-01-06T14:00:00", notes: "Interviewer unavailable"},
     *   {status: "SCHEDULED", changedBy: "recruiter@company.com", changedAt: "2024-01-08T10:00:00", notes: null},
     *   {status: "COMPLETED", changedBy: "interviewer@company.com", changedAt: "2024-01-08T11:30:00", notes: "Interview completed successfully"}
     * ]
     */
    @GetMapping("/{interviewId}")
    public ResponseEntity<List<InterviewStatusHistory>> getInterviewHistory(@PathVariable Long interviewId) {
        log.info("GET /api/history/interviews/{} - Fetching status history", interviewId);
        
        List<InterviewStatusHistory> history = 
            historyRepository.findByInterviewIdOrderByChangedAtAsc(interviewId);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * GET /api/history/interviews/recent - Get recent status changes
     * Query param: days (default: 7)
     * 
     * Use Case: Activity feed
     * Shows: "3 interviews rescheduled in the last 7 days"
     */
    @GetMapping("/recent")
    public ResponseEntity<List<InterviewStatusHistory>> getRecentInterviewChanges(
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("GET /api/history/interviews/recent - Last {} days", days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<InterviewStatusHistory> recentChanges = 
            historyRepository.findRecentChanges(since);
        
        return ResponseEntity.ok(recentChanges);
    }
    
    /**
     * GET /api/history/interviews/analytics/cancellations - Who cancels interviews most?
     * 
     * Use Case: Quality metrics
     * Identify problematic schedulers or patterns
     * 
     * Example Response:
     * [
     *   ["recruiter1@company.com", 15],
     *   ["recruiter2@company.com", 3],
     *   ["system", 2]
     * ]
     */
    @GetMapping("/analytics/cancellations")
    public ResponseEntity<List<Object[]>> getCancellationsByUser() {
        log.info("GET /api/history/interviews/analytics/cancellations");
        
        List<Object[]> cancellations = historyRepository.findCancellationsByUser();
        
        return ResponseEntity.ok(cancellations);
    }
}