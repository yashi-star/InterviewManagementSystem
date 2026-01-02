package com.interview.management.controller;

import com.interview.management.entity.AIScreening;
import com.interview.management.service.AIScreeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI Screening REST Controller - Updated
 * 
 * Endpoints for AI-powered resume screening using Ollama
 */
@RestController
@RequestMapping("/api/screenings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AIScreeningController {
    
    private final AIScreeningService screeningService;
    
    /**
     * POST /api/screenings/candidate/{candidateId} - Screen a candidate
     * Query params: jobDescription (optional)
     */
    @PostMapping("/candidate/{candidateId}")
    public ResponseEntity<AIScreening> screenCandidate(
            @PathVariable Long candidateId,
            @RequestParam(required = false) String jobDescription) {
        
        log.info("POST /api/screenings/candidate/{} - Initiating AI screening", candidateId);
        
        try {
            AIScreening screening = screeningService.screenCandidate(candidateId, jobDescription);
            return ResponseEntity.status(HttpStatus.CREATED).body(screening);
        } catch (Exception e) {
            log.error("Error screening candidate: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * POST /api/screenings/candidate/{candidateId}/async - Screen a candidate asynchronously
     * Returns immediately, screening happens in background
     */
    @PostMapping("/candidate/{candidateId}/async")
    public ResponseEntity<Map<String, String>> screenCandidateAsync(
            @PathVariable Long candidateId,
            @RequestParam(required = false) String jobDescription) {
        
        log.info("POST /api/screenings/candidate/{}/async - Starting async screening", candidateId);
        
        try {
            screeningService.screenCandidateAsync(candidateId, jobDescription);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "AI screening started in background");
            response.put("candidateId", candidateId.toString());
            response.put("status", "PROCESSING");
            
            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            log.error("Error starting async screening: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * POST /api/screenings/candidate/{candidateId}/rescreen - Re-screen a candidate
     */
    @PostMapping("/candidate/{candidateId}/rescreen")
    public ResponseEntity<AIScreening> rescreenCandidate(
            @PathVariable Long candidateId,
            @RequestParam(required = false) String jobDescription) {
        
        log.info("POST /api/screenings/candidate/{}/rescreen - Re-screening candidate", candidateId);
        
        try {
            AIScreening screening = screeningService.rescreenCandidate(candidateId, jobDescription);
            return ResponseEntity.status(HttpStatus.CREATED).body(screening);
        } catch (Exception e) {
            log.error("Error re-screening candidate: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * POST /api/screenings/bulk - Bulk screen multiple candidates
     * Request body: {"candidateIds": [1, 2, 3], "jobDescription": "..."}
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkScreenCandidates(
            @RequestBody BulkScreenRequest request) {
        
        log.info("POST /api/screenings/bulk - Screening {} candidates", request.getCandidateIds().size());
        
        try {
            List<CompletableFuture<AIScreening>> futures = 
                screeningService.bulkScreenCandidatesAsync(
                    request.getCandidateIds(), 
                    request.getJobDescription()
                );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk screening started");
            response.put("totalCandidates", request.getCandidateIds().size());
            response.put("status", "PROCESSING");
            
            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            log.error("Error in bulk screening", e);
            throw e;
        }
    }
    
    /**
     * GET /api/screenings/{id} - Get screening by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AIScreening> getScreeningById(@PathVariable Long id) {
        log.info("GET /api/screenings/{}", id);
        
        AIScreening screening = screeningService.getScreeningById(id);
        return ResponseEntity.ok(screening);
    }
    
    /**
     * GET /api/screenings/candidate/{candidateId} - Get all screenings for a candidate
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<AIScreening>> getScreeningsForCandidate(
            @PathVariable Long candidateId) {
        
        log.info("GET /api/screenings/candidate/{}", candidateId);
        
        List<AIScreening> screenings = screeningService.getScreeningsForCandidate(candidateId);
        return ResponseEntity.ok(screenings);
    }
    
    /**
     * GET /api/screenings/candidate/{candidateId}/latest - Get latest screening
     */
    @GetMapping("/candidate/{candidateId}/latest")
    public ResponseEntity<AIScreening> getLatestScreeningForCandidate(
            @PathVariable Long candidateId) {
        
        log.info("GET /api/screenings/candidate/{}/latest", candidateId);
        
        AIScreening screening = screeningService.getLatestScreeningForCandidate(candidateId);
        return ResponseEntity.ok(screening);
    }
    
    /**
     * GET /api/screenings/high-score - Get high-scoring candidates
     */
    @GetMapping("/high-score")
    public ResponseEntity<List<AIScreening>> getHighScoreCandidates(
            @RequestParam(defaultValue = "70") int minScore) {
        
        log.info("GET /api/screenings/high-score - minScore: {}", minScore);
        
        List<AIScreening> screenings = screeningService.getHighScoreCandidates(minScore);
        return ResponseEntity.ok(screenings);
    }
    
    /**
     * GET /api/screenings/date-range - Get screenings in date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<AIScreening>> getScreeningsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        log.info("GET /api/screenings/date-range - start: {}, end: {}", start, end);
        
        List<AIScreening> screenings = screeningService.getScreeningsInDateRange(start, end);
        return ResponseEntity.ok(screenings);
    }
    
    /**
     * GET /api/screenings/analytics/score-by-stage - Get average scores by stage
     */
    @GetMapping("/analytics/score-by-stage")
    public ResponseEntity<List<Object[]>> getAverageScoreByStage() {
        log.info("GET /api/screenings/analytics/score-by-stage");
        
        List<Object[]> analytics = screeningService.getAverageScoreByStage();
        return ResponseEntity.ok(analytics);
    }
    
    /**
     * GET /api/screenings/statistics - Get screening statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<AIScreeningService.ScreeningStatistics> getStatistics() {
        log.info("GET /api/screenings/statistics");
        
        AIScreeningService.ScreeningStatistics stats = screeningService.getScreeningStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * DELETE /api/screenings/{id} - Delete a screening
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long id) {
        log.info("DELETE /api/screenings/{}", id);
        
        screeningService.deleteScreening(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Request DTO for bulk screening
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkScreenRequest {
        private List<Long> candidateIds;
        private String jobDescription;
    }
}