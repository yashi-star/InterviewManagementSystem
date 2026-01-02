package com.interview.management.controller;

import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Dashboard REST Controller
 * 
 * Provides statistics and analytics endpoints for the recruiter dashboard
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * GET /api/dashboard - Get complete dashboard statistics
     * 
     * Returns:
     * - Total candidates
     * - Candidates this month
     * - Interviews scheduled today
     * - Pending feedback count
     * - Candidate distribution by stage
     * - Recent activity
     * - Top candidates by AI score
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        log.info("GET /api/dashboard - Fetching complete dashboard statistics");
        
        Map<String, Object> stats = dashboardService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * GET /api/dashboard/candidates/by-stage - Get candidate count by stage
     */
    @GetMapping("/candidates/by-stage")
    public ResponseEntity<Map<CandidateStage, Long>> getCandidateCountByStage() {
        log.info("GET /api/dashboard/candidates/by-stage");
        
        Map<CandidateStage, Long> counts = dashboardService.getCandidateCountByStage();
        return ResponseEntity.ok(counts);
    }
    
    /**
     * GET /api/dashboard/activity/recent - Get recent activity
     */
    @GetMapping("/activity/recent")
    public ResponseEntity<List<Object>> getRecentActivity(
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("GET /api/dashboard/activity/recent - days: {}", days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object> activity = dashboardService.getRecentActivity(since);
        
        return ResponseEntity.ok(activity);
    }
    
    /**
     * GET /api/dashboard/candidates/top-scored - Get top-scored candidates
     */
    @GetMapping("/candidates/top-scored")
    public ResponseEntity<List<Object>> getTopScoredCandidates(
            @RequestParam(defaultValue = "80") int minScore,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("GET /api/dashboard/candidates/top-scored - minScore: {}, limit: {}", minScore, limit);
        
        List<Object> candidates = dashboardService.getTopScoredCandidates(minScore, limit);
        return ResponseEntity.ok(candidates);
    }
    
    /**
     * GET /api/dashboard/interviews/statistics - Get interview statistics
     */
    @GetMapping("/interviews/statistics")
    public ResponseEntity<Map<String, Object>> getInterviewStatistics() {
        log.info("GET /api/dashboard/interviews/statistics");
        
        Map<String, Object> stats = dashboardService.getInterviewStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * GET /api/dashboard/screenings/statistics - Get AI screening statistics
     */
    @GetMapping("/screenings/statistics")
    public ResponseEntity<Map<String, Object>> getScreeningStatistics() {
        log.info("GET /api/dashboard/screenings/statistics");
        
        Map<String, Object> stats = dashboardService.getScreeningStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * GET /api/dashboard/feedback/statistics - Get feedback statistics
     */
    @GetMapping("/feedback/statistics")
    public ResponseEntity<Map<String, Object>> getFeedbackStatistics() {
        log.info("GET /api/dashboard/feedback/statistics");
        
        Map<String, Object> stats = dashboardService.getFeedbackStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * GET /api/dashboard/hiring-funnel - Get hiring funnel statistics
     */
    @GetMapping("/hiring-funnel")
    public ResponseEntity<Map<String, Object>> getHiringFunnelStatistics() {
        log.info("GET /api/dashboard/hiring-funnel");
        
        Map<String, Object> funnel = dashboardService.getHiringFunnelStatistics();
        return ResponseEntity.ok(funnel);
    }
    
    /**
     * GET /api/dashboard/candidates/recent - Get recent candidates
     */
    @GetMapping("/candidates/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentCandidates(
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("GET /api/dashboard/candidates/recent - days: {}", days);
        
        List<Map<String, Object>> candidates = dashboardService.getRecentCandidates(days);
        return ResponseEntity.ok(candidates);
    }
    
    /**
     * GET /api/dashboard/interviews/upcoming - Get upcoming interviews
     */
    @GetMapping("/interviews/upcoming")
    public ResponseEntity<List<Map<String, Object>>> getUpcomingInterviews(
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("GET /api/dashboard/interviews/upcoming - days: {}", days);
        
        List<Map<String, Object>> interviews = dashboardService.getUpcomingInterviews(days);
        return ResponseEntity.ok(interviews);
    }
}