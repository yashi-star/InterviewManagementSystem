package com.interview.management.controller;

import com.interview.management.entity.Feedback;
import com.interview.management.entity.enums.Recommendation;
import com.interview.management.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feedback REST Controller
 * 
 * Endpoints for feedback collection and management
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    
    /**
     * POST /api/feedback - Submit feedback
     */
    @PostMapping
    public ResponseEntity<Feedback> submitFeedback(
            @RequestParam Long interviewId,
            @RequestParam Long interviewerId,
            @RequestParam Integer technicalScore,
            @RequestParam Integer communicationScore,
            @RequestParam Integer problemSolvingScore,
            @RequestParam(required = false) Integer culturalFitScore,
            @RequestParam(required = false) String strengths,
            @RequestParam(required = false) String weaknesses,
            @RequestParam(required = false) String additionalComments,
            @RequestParam Recommendation recommendation) {
        
        log.info("POST /api/feedback - Submitting feedback for interview: {}", interviewId);
        
        try {
            Feedback feedback = feedbackService.submitFeedback(
                    interviewId, interviewerId,
                    technicalScore, communicationScore, problemSolvingScore, culturalFitScore,
                    strengths, weaknesses, additionalComments, recommendation);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("State error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error submitting feedback", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/feedback/{id} - Get feedback by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
        log.info("GET /api/feedback/{}", id);
        
        try {
            Feedback feedback = feedbackService.getFeedbackById(id);
            return ResponseEntity.ok(feedback);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/feedback/interview/{interviewId} - Get all feedback for an interview
     */
    @GetMapping("/interview/{interviewId}")
    public ResponseEntity<List<Feedback>> getFeedbackForInterview(@PathVariable Long interviewId) {
        log.info("GET /api/feedback/interview/{}", interviewId);
        
        List<Feedback> feedbacks = feedbackService.getFeedbackForInterview(interviewId);
        return ResponseEntity.ok(feedbacks);
    }
    
    /**
     * GET /api/feedback/interviewer/{interviewerId} - Get feedback by interviewer
     */
    @GetMapping("/interviewer/{interviewerId}")
    public ResponseEntity<List<Feedback>> getFeedbackByInterviewer(@PathVariable Long interviewerId) {
        log.info("GET /api/feedback/interviewer/{}", interviewerId);
        
        List<Feedback> feedbacks = feedbackService.getFeedbackByInterviewer(interviewerId);
        return ResponseEntity.ok(feedbacks);
    }
    
    /**
     * GET /api/feedback/candidate/{candidateId}/averages - Get average scores for candidate
     */
    @GetMapping("/candidate/{candidateId}/averages")
    public ResponseEntity<Map<String, Double>> getAverageScoresForCandidate(
            @PathVariable Long candidateId) {
        
        log.info("GET /api/feedback/candidate/{}/averages", candidateId);
        
        try {
            Map<String, Double> averages = feedbackService.getAverageScoresForCandidate(candidateId);
            return ResponseEntity.ok(averages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/feedback/positive - Get positive feedback (STRONG_HIRE or HIRE)
     */
    @GetMapping("/positive")
    public ResponseEntity<List<Feedback>> getPositiveFeedback() {
        log.info("GET /api/feedback/positive");
        
        List<Feedback> feedbacks = feedbackService.getPositiveFeedback();
        return ResponseEntity.ok(feedbacks);
    }
    
    /**
     * GET /api/feedback/interviewer/{interviewerId}/statistics - Get interviewer stats
     */
    @GetMapping("/interviewer/{interviewerId}/statistics")
    public ResponseEntity<Map<String, Object>> getInterviewerStatistics(
            @PathVariable Long interviewerId) {
        
        log.info("GET /api/feedback/interviewer/{}/statistics", interviewerId);
        
        try {
            Map<String, Object> stats = feedbackService.getInterviewerStatistics(interviewerId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/feedback/{id}/overall-score - Calculate overall score
     */
    @GetMapping("/{id}/overall-score")
    public ResponseEntity<Map<String, Double>> getOverallScore(@PathVariable Long id) {
        log.info("GET /api/feedback/{}/overall-score", id);
        
        try {
            Double overallScore = feedbackService.calculateOverallScore(id);
            return ResponseEntity.ok(Map.of("overallScore", overallScore));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * PUT /api/feedback/{id} - Update feedback
     */
    @PutMapping("/{id}")
    public ResponseEntity<Feedback> updateFeedback(
            @PathVariable Long id,
            @RequestParam(required = false) Integer technicalScore,
            @RequestParam(required = false) Integer communicationScore,
            @RequestParam(required = false) Integer problemSolvingScore,
            @RequestParam(required = false) Integer culturalFitScore,
            @RequestParam(required = false) String strengths,
            @RequestParam(required = false) String weaknesses,
            @RequestParam(required = false) String additionalComments,
            @RequestParam(required = false) Recommendation recommendation) {
        
        log.info("PUT /api/feedback/{} - Updating feedback", id);
        
        try {
            Feedback updated = feedbackService.updateFeedback(
                    id, technicalScore, communicationScore, problemSolvingScore,
                    culturalFitScore, strengths, weaknesses, additionalComments, recommendation);
            
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException e) {
            log.error("Error updating feedback: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * DELETE /api/feedback/{id} - Delete feedback
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        log.info("DELETE /api/feedback/{}", id);
        
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}