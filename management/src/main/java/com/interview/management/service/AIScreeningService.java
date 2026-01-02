package com.interview.management.service;

import com.interview.management.dto.AIAnalysisResult;
import com.interview.management.entity.AIScreening;
import com.interview.management.entity.Candidate;
import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.exception.AIScreeningException;
import com.interview.management.exception.ResourceNotFoundException;
import com.interview.management.exception.ValidationException;
import com.interview.management.repository.AIScreeningRepository;
import com.interview.management.repository.CandidateRepository;
import com.interview.management.service.ai.AIResumeAnalyzerService;
import com.interview.management.service.ai.ResumeParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI Screening Service - Updated with Real AI Integration
 * Handles AI-powered resume analysis using Spring AI + Ollama
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AIScreeningService {
    
    private final AIScreeningRepository screeningRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeParserService resumeParserService;
    private final AIResumeAnalyzerService aiResumeAnalyzerService;
    
    @Value("${spring.ai.ollama.chat.options.model:llama2}")
    private String aiModelName;
    
    /**
     * Perform AI screening for a candidate
     * This now uses real AI instead of mock data!
     */
    public AIScreening screenCandidate(Long candidateId) {
        return screenCandidate(candidateId, null);
    }
    
    /**
     * Perform AI screening for a candidate with optional job description
     */
    public AIScreening screenCandidate(Long candidateId, String jobDescription) {
        log.info("Starting AI screening for candidate: {}", candidateId);
        long startTime = System.currentTimeMillis();
        
        // Get candidate
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));
        
        // Validate candidate has resume
        if (candidate.getResumePath() == null || candidate.getResumePath().isEmpty()) {
            throw new ValidationException("resume", "Candidate has no resume uploaded");
        }
        
        try {
            // Step 1: Extract text from resume
            log.info("Step 1: Extracting text from resume");
            String resumeText = resumeParserService.extractTextFromResume(candidate.getResumePath());
            
            // Validate resume content
            if (!resumeParserService.hasValidContent(resumeText)) {
                throw new ValidationException("resume", "Resume does not contain valid content");
            }
            
            log.info("Resume text extracted: {} characters", resumeText.length());
            
            // Step 2: Analyze with AI
            log.info("Step 2: Analyzing resume with AI (model: {})", aiModelName);
            AIAnalysisResult aiResult = aiResumeAnalyzerService.analyzeResume(resumeText, jobDescription);
            
            long processingTime = System.currentTimeMillis() - startTime;
            aiResult.setProcessingTimeMs(processingTime);
            aiResult.setModelUsed(aiModelName);
            
            log.info("AI analysis complete in {}ms - Score: {}", processingTime, aiResult.getMatchScore());
            
            // Step 3: Create screening record
            AIScreening screening = AIScreening.builder()
                    .candidate(candidate)
                    .skillsMatched(aiResult.getSkillsMatched())
                    .experienceYears(aiResult.getExperienceYears())
                    .educationLevel(aiResult.getEducationLevel())
                    .culturalFit(aiResult.getCulturalFit())
                    .matchScore(aiResult.getMatchScore())
                    .analysisText(aiResult.getAnalysisText())
                    .aiModelUsed(aiModelName)
                    .build();
            
            // Save screening
            AIScreening saved = screeningRepository.save(screening);
            
            // Update candidate stage to SCREENING
            if (candidate.getCurrentStage() == CandidateStage.APPLIED) {
                candidate.updateStage(
                    CandidateStage.SCREENING, 
                    "AI_SYSTEM", 
                    String.format("Automated AI screening completed. Score: %d/100", aiResult.getMatchScore())
                );
                candidateRepository.save(candidate);
            }
            
            log.info("AI screening saved successfully for candidate: {}. Score: {}", 
                    candidateId, saved.getMatchScore());
            
            return saved;
            
        } catch (AIScreeningException e) {
            log.error("AI screening failed for candidate: {}", candidateId, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during AI screening for candidate: {}", candidateId, e);
            throw new AIScreeningException("AI screening process failed", e);
        }
    }
    
    /**
     * Async AI screening - runs in background
     * Use this for bulk screening to avoid blocking
     */
    @Async
    public CompletableFuture<AIScreening> screenCandidateAsync(Long candidateId, String jobDescription) {
        log.info("Starting async AI screening for candidate: {}", candidateId);
        
        try {
            AIScreening screening = screenCandidate(candidateId, jobDescription);
            return CompletableFuture.completedFuture(screening);
        } catch (Exception e) {
            log.error("Async AI screening failed for candidate: {}", candidateId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Get screening by ID
     */
    @Transactional(readOnly = true)
    public AIScreening getScreeningById(Long id) {
        log.debug("Fetching screening: {}", id);
        return screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AIScreening", id));
    }
    
    /**
     * Get all screenings for a candidate
     */
    @Transactional(readOnly = true)
    public List<AIScreening> getScreeningsForCandidate(Long candidateId) {
        log.debug("Fetching screenings for candidate: {}", candidateId);
        
        // Verify candidate exists
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException("Candidate", candidateId);
        }
        
        return screeningRepository.findByCandidateIdOrderByScreenedAtDesc(candidateId);
    }
    
    /**
     * Get latest screening for a candidate
     */
    @Transactional(readOnly = true)
    public AIScreening getLatestScreeningForCandidate(Long candidateId) {
        log.debug("Fetching latest screening for candidate: {}", candidateId);
        return screeningRepository.findLatestScreeningForCandidate(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No screening found for candidate: " + candidateId));
    }
    
    /**
     * Get high-scoring candidates
     */
    @Transactional(readOnly = true)
    public List<AIScreening> getHighScoreCandidates(int minScore) {
        log.debug("Fetching candidates with score >= {}", minScore);
        return screeningRepository.findHighScoreCandidates(minScore);
    }
    
    /**
     * Get screenings in date range
     */
    @Transactional(readOnly = true)
    public List<AIScreening> getScreeningsInDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching screenings between {} and {}", start, end);
        return screeningRepository.findScreeningsInDateRange(start, end);
    }
    
    /**
     * Get average score by candidate stage
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageScoreByStage() {
        log.debug("Fetching average scores by stage");
        return screeningRepository.getAverageScoreByStage();
    }
    
    /**
     * Re-screen candidate (if resume updated)
     */
    public AIScreening rescreenCandidate(Long candidateId, String jobDescription) {
        log.info("Re-screening candidate: {}", candidateId);
        
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", candidateId));
        
        // Check if there's an existing screening
        screeningRepository.findLatestScreeningForCandidate(candidateId)
                .ifPresent(existing -> 
                    log.info("Previous screening found with score: {}. Performing new screening.", 
                            existing.getMatchScore())
                );
        
        return screenCandidate(candidateId, jobDescription);
    }
    
    /**
     * Delete screening
     */
    public void deleteScreening(Long screeningId) {
        log.info("Deleting screening: {}", screeningId);
        
        AIScreening screening = getScreeningById(screeningId);
        screeningRepository.delete(screening);
        
        log.info("Screening {} deleted successfully", screeningId);
    }
    
    /**
     * Bulk screen multiple candidates asynchronously
     * Returns immediately, screening happens in background
     */
    public List<CompletableFuture<AIScreening>> bulkScreenCandidatesAsync(
            List<Long> candidateIds, String jobDescription) {
        
        log.info("Starting bulk async screening for {} candidates", candidateIds.size());
        
        return candidateIds.stream()
                .map(id -> screenCandidateAsync(id, jobDescription))
                .toList();
    }
    
    /**
     * Get screening statistics
     */
    @Transactional(readOnly = true)
    public ScreeningStatistics getScreeningStatistics() {
        long totalScreenings = screeningRepository.count();
        List<AIScreening> highScorers = screeningRepository.findHighScoreCandidates(80);
        
        // Calculate average score
        List<AIScreening> allScreenings = screeningRepository.findAll();
        double avgScore = allScreenings.stream()
                .mapToInt(AIScreening::getMatchScore)
                .average()
                .orElse(0.0);
        
        return ScreeningStatistics.builder()
                .totalScreenings(totalScreenings)
                .highScoreCandidates(highScorers.size())
                .averageScore(avgScore)
                .screeningsToday(getScreeningsToday())
                .build();
    }
    
    /**
     * Get screenings performed today
     */
    private long getScreeningsToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        return screeningRepository.findScreeningsInDateRange(startOfDay, endOfDay).size();
    }
    
    /**
     * DTO for screening statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class ScreeningStatistics {
        private Long totalScreenings;
        private Integer highScoreCandidates;
        private Double averageScore;
        private Long screeningsToday;
    }
}