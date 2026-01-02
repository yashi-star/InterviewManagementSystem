package com.interview.management.controller;

import com.interview.management.entity.Interviewer;
import com.interview.management.service.InterviewerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interviewer REST Controller
 * 
 * Endpoints for interviewer management
 */
@RestController
@RequestMapping("/api/interviewers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InterviewerController {
    
    private final InterviewerService interviewerService;
    
    /**
     * POST /api/interviewers - Create new interviewer
     */
    @PostMapping
    public ResponseEntity<Interviewer> createInterviewer(@RequestBody Interviewer interviewer) {
        log.info("POST /api/interviewers - Creating interviewer: {}", interviewer.getEmail());
        
        try {
            Interviewer created = interviewerService.createInterviewer(interviewer);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * GET /api/interviewers/{id} - Get interviewer by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Interviewer> getInterviewerById(@PathVariable Long id) {
        log.info("GET /api/interviewers/{}", id);
        
        try {
            Interviewer interviewer = interviewerService.getInterviewerById(id);
            return ResponseEntity.ok(interviewer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/interviewers/email/{email} - Get interviewer by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Interviewer> getInterviewerByEmail(@PathVariable String email) {
        log.info("GET /api/interviewers/email/{}", email);
        
        try {
            Interviewer interviewer = interviewerService.getInterviewerByEmail(email);
            return ResponseEntity.ok(interviewer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/interviewers - Get all interviewers
     */
    @GetMapping
    public ResponseEntity<List<Interviewer>> getAllInterviewers() {
        log.info("GET /api/interviewers");
        
        List<Interviewer> interviewers = interviewerService.getAllInterviewers();
        return ResponseEntity.ok(interviewers);
    }
    
    /**
     * GET /api/interviewers/department/{department} - Get interviewers by department
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Interviewer>> getInterviewersByDepartment(
            @PathVariable String department) {
        
        log.info("GET /api/interviewers/department/{}", department);
        
        List<Interviewer> interviewers = interviewerService.getInterviewersByDepartment(department);
        return ResponseEntity.ok(interviewers);
    }
    
    /**
     * GET /api/interviewers/expertise/{skill} - Find interviewers by expertise
     */
    @GetMapping("/expertise/{skill}")
    public ResponseEntity<List<Interviewer>> findInterviewersByExpertise(@PathVariable String skill) {
        log.info("GET /api/interviewers/expertise/{}", skill);
        
        List<Interviewer> interviewers = interviewerService.findInterviewersByExpertise(skill);
        return ResponseEntity.ok(interviewers);
    }
    
    /**
     * GET /api/interviewers/available - Find available interviewers in time slot
     */
    @GetMapping("/available")
    public ResponseEntity<List<Interviewer>> findAvailableInterviewers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("GET /api/interviewers/available - startTime: {}, endTime: {}", startTime, endTime);
        
        List<Interviewer> interviewers = interviewerService.findAvailableInterviewers(startTime, endTime);
        return ResponseEntity.ok(interviewers);
    }
    
    /**
     * GET /api/interviewers/{id}/is-available - Check if interviewer is available
     */
    @GetMapping("/{id}/is-available")
    public ResponseEntity<Map<String, Boolean>> isInterviewerAvailable(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("GET /api/interviewers/{}/is-available", id);
        
        try {
            boolean available = interviewerService.isInterviewerAvailable(id, startTime, endTime);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * PUT /api/interviewers/{id} - Update interviewer
     */
    @PutMapping("/{id}")
    public ResponseEntity<Interviewer> updateInterviewer(
            @PathVariable Long id,
            @RequestBody Interviewer updates) {
        
        log.info("PUT /api/interviewers/{} - Updating interviewer", id);
        
        try {
            Interviewer updated = interviewerService.updateInterviewer(id, updates);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * DELETE /api/interviewers/{id} - Delete interviewer
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterviewer(@PathVariable Long id) {
        log.info("DELETE /api/interviewers/{}", id);
        
        try {
            interviewerService.deleteInterviewer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Cannot delete: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}