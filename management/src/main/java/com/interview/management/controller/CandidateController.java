package com.interview.management.controller;

import com.interview.management.entity.Candidate;
import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Candidate REST Controller
 * 
 * Endpoints for candidate management
 */
@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Configure properly in production
public class CandidateController {
    
    private final CandidateService candidateService;
    
    /**
     * POST /api/candidates - Create new candidate with resume
     * Content-Type: multipart/form-data
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Candidate> createCandidate(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "resume", required = false) MultipartFile resume) {
        
        log.info("POST /api/candidates - Creating candidate: {}", email);
        
        try {
            Candidate candidate = Candidate.builder()
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .build();
            
            Candidate created = candidateService.createCandidate(candidate, resume);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating candidate", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/candidates/{id} - Get candidate by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        log.info("GET /api/candidates/{} - Fetching candidate", id);
        
        try {
            Candidate candidate = candidateService.getCandidateById(id);
            return ResponseEntity.ok(candidate);
        } catch (IllegalArgumentException e) {
            log.error("Candidate not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/candidates - Get all candidates with pagination
     * Query params: page, size, sortBy, sortDir
     */
    @GetMapping
    public ResponseEntity<Page<Candidate>> getAllCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        log.info("GET /api/candidates - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Candidate> candidates = candidateService.getAllCandidates(pageable);
        
        return ResponseEntity.ok(candidates);
    }
    
    /**
     * GET /api/candidates/email/{email} - Get candidate by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Candidate> getCandidateByEmail(@PathVariable String email) {
        log.info("GET /api/candidates/email/{}", email);
        
        try {
            Candidate candidate = candidateService.getCandidateByEmail(email);
            return ResponseEntity.ok(candidate);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/candidates/stage/{stage} - Get candidates by stage
     */
    @GetMapping("/stage/{stage}")
    public ResponseEntity<Page<Candidate>> getCandidatesByStage(
            @PathVariable CandidateStage stage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/candidates/stage/{}", stage);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Candidate> candidates = candidateService.getCandidatesByStage(stage, pageable);
        
        return ResponseEntity.ok(candidates);
    }
    
    /**
     * GET /api/candidates/search - Search candidates
     * Query params: name, email, stage, page, size
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Candidate>> searchCandidates(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) CandidateStage stage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/candidates/search - name: {}, email: {}, stage: {}", name, email, stage);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Candidate> results = candidateService.searchCandidates(name, email, stage, pageable);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * PUT /api/candidates/{id} - Update candidate details
     */
    @PutMapping("/{id}")
    public ResponseEntity<Candidate> updateCandidate(
            @PathVariable Long id,
            @RequestBody Candidate updates) {
        
        log.info("PUT /api/candidates/{} - Updating candidate", id);
        
        try {
            Candidate updated = candidateService.updateCandidate(id, updates);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * PUT /api/candidates/{id}/stage - Update candidate stage
     */
    @PutMapping("/{id}/stage")
    public ResponseEntity<Candidate> updateCandidateStage(
            @PathVariable Long id,
            @RequestParam CandidateStage newStage,
            @RequestParam String changedBy,
            @RequestParam(required = false) String reason) {
        
        log.info("PUT /api/candidates/{}/stage - Moving to: {}", id, newStage);
        
        try {
            Candidate updated = candidateService.updateCandidateStage(id, newStage, changedBy, reason);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Error updating stage: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * DELETE /api/candidates/{id} - Delete candidate
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        log.info("DELETE /api/candidates/{}", id);
        
        try {
            candidateService.deleteCandidate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/candidates/without-screening - Get candidates needing AI screening
     */
    @GetMapping("/without-screening")
    public ResponseEntity<List<Candidate>> getCandidatesWithoutScreening() {
        log.info("GET /api/candidates/without-screening");
        
        List<Candidate> candidates = candidateService.getCandidatesWithoutScreening();
        return ResponseEntity.ok(candidates);
    }
    
    /**
     * GET /api/candidates/top-scored - Get top candidates by AI score
     */
    @GetMapping("/top-scored")
    public ResponseEntity<List<Candidate>> getTopCandidates(
            @RequestParam(defaultValue = "70") int minScore,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("GET /api/candidates/top-scored - minScore: {}, limit: {}", minScore, limit);
        
        List<Candidate> candidates = candidateService.getTopCandidatesByAIScore(minScore, limit);
        return ResponseEntity.ok(candidates);
    }
    
    /**
     * GET /api/candidates/statistics - Get candidate statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCandidateStatistics() {
        log.info("GET /api/candidates/statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("countByStage", candidateService.getCandidateCountByStage());
        stats.put("totalThisMonth", candidateService.countCandidatesThisMonth());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * GET /api/candidates/recent - Get candidates from last N days
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Candidate>> getRecentCandidates(
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("GET /api/candidates/recent - days: {}", days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Candidate> candidates = candidateService.getCandidatesCreatedAfter(since);
        
        return ResponseEntity.ok(candidates);
    }
    
    /**
     * GET /api/candidates/{id}/resume-path - Get resume file path
     */
    @GetMapping("/{id}/resume-path")
    public ResponseEntity<Map<String, String>> getResumePath(@PathVariable Long id) {
        log.info("GET /api/candidates/{}/resume-path", id);
        
        try {
            String path = candidateService.getResumePath(id);
            Map<String, String> response = new HashMap<>();
            response.put("resumePath", path);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}