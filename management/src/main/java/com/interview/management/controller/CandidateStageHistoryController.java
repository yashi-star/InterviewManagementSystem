package com.interview.management.controller;

import com.interview.management.entity.CandidateStageHistory;
import com.interview.management.entity.InterviewStatusHistory;
import com.interview.management.repository.CandidateStageHistoryRepository;
import com.interview.management.repository.InterviewStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Candidate Stage History Controller
 * 
 * READ-ONLY endpoints for viewing candidate stage history
 * History records are created automatically in CandidateService, not manually
 */
@RestController
@RequestMapping("/api/history/candidates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CandidateStageHistoryController {
    
    private final CandidateStageHistoryRepository historyRepository;
    
    /**
     * GET /api/history/candidates/{candidateId} - Get complete journey for a candidate
     * 
     * Use Case: Display timeline in candidate profile
     * Example Response:
     * [
     *   {id: 1, fromStage: null, toStage: "APPLIED", changedBy: "system", changedAt: "2024-01-01T10:00:00"},
     *   {id: 2, fromStage: "APPLIED", toStage: "SCREENING", changedBy: "recruiter@company.com", changedAt: "2024-01-02T14:30:00"},
     *   {id: 3, fromStage: "SCREENING", toStage: "INTERVIEW_SCHEDULED", changedBy: "recruiter@company.com", changedAt: "2024-01-05T09:00:00"}
     * ]
     */
    @GetMapping("/{candidateId}")
    public ResponseEntity<List<CandidateStageHistory>> getCandidateHistory(@PathVariable Long candidateId) {
        log.info("GET /api/history/candidates/{} - Fetching stage history", candidateId);
        
        List<CandidateStageHistory> history = 
            historyRepository.findByCandidateIdOrderByChangedAtAsc(candidateId);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * GET /api/history/candidates/recent - Get recent stage changes across all candidates
     * Query param: days (default: 7)
     * 
     * Use Case: Dashboard "Recent Activity" widget
     * Shows: "John Doe moved from SCREENING to INTERVIEW_SCHEDULED by recruiter@company.com"
     */
    @GetMapping("/recent")
    public ResponseEntity<List<CandidateStageHistory>> getRecentStageChanges(
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("GET /api/history/candidates/recent - Last {} days", days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<CandidateStageHistory> recentChanges = 
            historyRepository.findRecentStageChanges(since);
        
        return ResponseEntity.ok(recentChanges);
    }
    
    /**
     * GET /api/history/candidates/analytics/time-in-stage - Get average time in each stage
     * 
     * Use Case: Process optimization dashboard
     * Shows: "Candidates spend average 5.2 days in SCREENING stage"
     */
    @GetMapping("/analytics/time-in-stage")
    public ResponseEntity<List<Object[]>> getAverageTimeInEachStage() {
        log.info("GET /api/history/candidates/analytics/time-in-stage");
        
        List<Object[]> analytics = historyRepository.getAverageTimeInEachStage();
        
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * GET /api/history/candidates/stage/{stage}/today - Candidates who moved to a stage today
     * 
     * Use Case: Daily summary email
     * "5 candidates moved to INTERVIEW_SCHEDULED today"
     */
    @GetMapping("/stage/{stage}/today")
    public ResponseEntity<List<CandidateStageHistory>> getCandidatesMovedToStageToday(
            @PathVariable String stage) {
        
        log.info("GET /api/history/candidates/stage/{}/today", stage);
        
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<CandidateStageHistory> movedToday = 
            historyRepository.findCandidatesMovedToStageToday(
                com.interview.management.entity.enums.CandidateStage.valueOf(stage), 
                today
            );
        
        return ResponseEntity.ok(movedToday);
    }
}
